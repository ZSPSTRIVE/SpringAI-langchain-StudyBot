#!/usr/bin/env python3
"""Build interview-oriented RAG seed files from Wikipedia.

The script combines two strategies:
1. Crawl broad category trees for each subject domain.
2. Query high-frequency interview topics as direct page searches.

Output format is JSONL files under:
    qa-system-backend/src/main/resources/knowledge/general/
"""

from __future__ import annotations

import argparse
import json
import re
import sys
import time
import urllib.parse
import urllib.request
from collections import OrderedDict, deque
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Dict, Iterable, List

import requests


API_URL = "https://en.wikipedia.org/w/api.php"
USER_AGENT = "qa-system-rag-seeder/1.0 (https://github.com/openai)"
DEFAULT_BATCH_SIZE = 20
DEFAULT_CATEGORY_DEPTH = 1
DEFAULT_TARGET_MULTIPLIER = 3
SLEEP_SECONDS = 0.05

EXCLUDED_TITLE_PREFIXES = (
    "list of ",
    "outline of ",
    "comparison of ",
    "timeline of ",
    "history of ",
    "glossary of ",
    "index of ",
)
EXCLUDED_TITLE_KEYWORDS = (
    "(disambiguation)",
    "template:",
    "category:",
    "portal:",
)


@dataclass(frozen=True)
class DomainSpec:
    code: str
    categories: List[str]
    search_terms: List[str]
    relevance_terms: List[str]
    target_count: int


DOMAIN_SPECS: List[DomainSpec] = [
    DomainSpec(
        code="data_structures",
        categories=[
            "Category:Data structures",
            "Category:Abstract data types",
            "Category:Trees (data structures)",
            "Category:Graph theory objects",
        ],
        search_terms=[
            "array",
            "linked list",
            "stack",
            "queue",
            "heap",
            "hash table",
            "binary search tree",
            "red-black tree",
            "B-tree",
            "trie",
            "segment tree",
            "disjoint-set data structure",
            "skip list",
        ],
        relevance_terms=[
            "computer science",
            "data structure",
            "abstract data type",
            "tree data",
            "graph",
            "node",
            "heap",
            "hash table",
            "linked list",
            "pointer",
        ],
        target_count=320,
    ),
    DomainSpec(
        code="algorithms",
        categories=[
            "Category:Algorithms",
            "Category:Graph algorithms",
            "Category:Search algorithms",
            "Category:Sorting algorithms",
            "Category:Computational problems",
        ],
        search_terms=[
            "binary search",
            "quicksort",
            "merge sort",
            "dynamic programming",
            "greedy algorithm",
            "backtracking",
            "breadth-first search",
            "depth-first search",
            "Dijkstra's algorithm",
            "topological sorting",
            "minimum spanning tree",
            "sliding window protocol",
        ],
        relevance_terms=[
            "algorithm",
            "computer science",
            "sorting",
            "search",
            "optimization",
            "graph",
            "dynamic programming",
            "complexity",
        ],
        target_count=320,
    ),
    DomainSpec(
        code="computer_networks",
        categories=[
            "Category:Computer networking",
            "Category:Network protocols",
            "Category:Internet protocols",
            "Category:Routing",
            "Category:Application layer protocols",
        ],
        search_terms=[
            "TCP",
            "UDP",
            "HTTP",
            "HTTPS",
            "DNS",
            "TLS",
            "OSI model",
            "congestion control",
            "flow control (data)",
            "load balancing (computing)",
            "network address translation",
            "cookie (internet)",
        ],
        relevance_terms=[
            "network",
            "protocol",
            "internet",
            "packet",
            "router",
            "switch",
            "tcp",
            "udp",
            "http",
            "dns",
        ],
        target_count=360,
    ),
    DomainSpec(
        code="operating_systems",
        categories=[
            "Category:Operating systems",
            "Category:Concurrent computing",
            "Category:Computer memory",
            "Category:Synchronization",
            "Category:Virtual memory",
        ],
        search_terms=[
            "process (computing)",
            "thread (computing)",
            "deadlock",
            "semaphore (programming)",
            "mutex",
            "virtual memory",
            "paging",
            "context switch",
            "system call",
            "CPU scheduling",
            "memory management",
            "inter-process communication",
        ],
        relevance_terms=[
            "operating system",
            "kernel",
            "process",
            "thread",
            "memory",
            "scheduling",
            "synchronization",
            "virtual memory",
            "system call",
        ],
        target_count=320,
    ),
    DomainSpec(
        code="databases",
        categories=[
            "Category:Databases",
            "Category:Database management systems",
            "Category:Data modeling",
            "Category:Transaction processing",
            "Category:SQL",
        ],
        search_terms=[
            "database index",
            "B+ tree",
            "transaction processing",
            "isolation (database systems)",
            "MVCC",
            "normalization (database)",
            "join (SQL)",
            "database query optimization",
            "CAP theorem",
            "sharding",
            "replication (computing)",
            "acid",
        ],
        relevance_terms=[
            "database",
            "sql",
            "transaction",
            "query",
            "index",
            "relational",
            "replication",
            "storage",
            "schema",
        ],
        target_count=320,
    ),
    DomainSpec(
        code="computer_architecture",
        categories=[
            "Category:Computer architecture",
            "Category:Central processing unit",
            "Category:Computer memory",
            "Category:Computer buses",
            "Category:Microarchitecture",
        ],
        search_terms=[
            "cache replacement policies",
            "instruction pipeline",
            "branch predictor",
            "superscalar processor",
            "memory hierarchy",
            "RISC",
            "CISC",
            "bus (computing)",
            "translation lookaside buffer",
            "out-of-order execution",
            "SIMD",
        ],
        relevance_terms=[
            "computer",
            "processor",
            "cpu",
            "cache",
            "memory",
            "instruction",
            "pipeline",
            "architecture",
            "microarchitecture",
        ],
        target_count=280,
    ),
    DomainSpec(
        code="signals_and_systems",
        categories=[
            "Category:Signal processing",
            "Category:Systems theory",
            "Category:Control theory",
            "Category:Transforms",
            "Category:Linear systems",
        ],
        search_terms=[
            "signal",
            "linear time-invariant system",
            "convolution",
            "Fourier transform",
            "Laplace transform",
            "z-transform",
            "transfer function",
            "sampling theorem",
            "impulse response",
            "frequency response",
        ],
        relevance_terms=[
            "fourier",
            "laplace",
            "convolution",
            "frequency",
            "time domain",
            "transform",
            "impulse response",
            "linear system",
            "transfer function",
            "response",
        ],
        target_count=280,
    ),
    DomainSpec(
        code="digital_signal_processing",
        categories=[
            "Category:Digital signal processing",
            "Category:Filter theory",
            "Category:Fourier analysis",
            "Category:Audio signal processing",
            "Category:Digital filters",
        ],
        search_terms=[
            "fast Fourier transform",
            "finite impulse response",
            "infinite impulse response",
            "window function",
            "aliasing",
            "quantization (signal processing)",
            "sampling rate",
            "digital filter",
            "spectral density",
            "signal reconstruction",
        ],
        relevance_terms=[
            "digital signal",
            "filter",
            "sampling",
            "quantization",
            "fourier",
            "spectrum",
            "aliasing",
            "reconstruction",
        ],
        target_count=280,
    ),
    DomainSpec(
        code="java_backend",
        categories=[
            "Category:Java platform",
            "Category:Java enterprise platform",
            "Category:Spring Framework",
            "Category:Concurrent computing",
            "Category:Distributed computing",
        ],
        search_terms=[
            "Java virtual machine",
            "Java memory model",
            "Garbage collection (computer science)",
            "Spring Framework",
            "Spring Boot",
            "dependency injection",
            "message queue",
            "distributed transaction",
            "circuit breaker design pattern",
            "rate limiting",
            "Redis",
            "Apache Kafka",
        ],
        relevance_terms=[
            "java",
            "jvm",
            "spring",
            "garbage collection",
            "memory management",
            "concurrency",
            "distributed",
            "redis",
            "kafka",
        ],
        target_count=320,
    ),
]


class MediaWikiClient:
    def __init__(self, api_url: str = API_URL, user_agent: str = USER_AGENT) -> None:
        self.api_url = api_url
        self.user_agent = user_agent
        self.session = requests.Session()
        self.session.headers.update({"User-Agent": self.user_agent})

    def get_json(self, params: Dict[str, str], retries: int = 3) -> Dict:
        last_error = None
        for attempt in range(1, retries + 1):
            try:
                response = self.session.get(self.api_url, params=params, timeout=30)
                response.raise_for_status()
                return response.json()
            except Exception as exc:  # noqa: BLE001
                last_error = exc
                if attempt == retries:
                    raise
                time.sleep(0.5 * attempt)
        raise RuntimeError(f"MediaWiki request failed: {last_error}")

    def category_members(self, category: str, max_depth: int, max_titles: int) -> List[str]:
        queue = deque([(category, 0)])
        visited_categories = set()
        titles = OrderedDict()

        while queue:
            current_category, depth = queue.popleft()
            if current_category in visited_categories or depth > max_depth:
                continue
            visited_categories.add(current_category)

            continuation = None
            while True:
                params = {
                    "action": "query",
                    "list": "categorymembers",
                    "cmtitle": current_category,
                    "cmlimit": "500",
                    "format": "json",
                }
                if continuation:
                    params["cmcontinue"] = continuation
                data = self.get_json(params)

                members = data.get("query", {}).get("categorymembers", [])
                for member in members:
                    namespace = member.get("ns")
                    title = member.get("title", "")
                    if namespace == 0 and is_relevant_title(title):
                        titles.setdefault(title, None)
                        if len(titles) >= max_titles:
                            return list(titles.keys())
                    elif namespace == 14 and depth < max_depth:
                        queue.append((title, depth + 1))

                continuation = data.get("continue", {}).get("cmcontinue")
                if not continuation:
                    break
                time.sleep(SLEEP_SECONDS)

        return list(titles.keys())

    def search(self, term: str, limit: int = 6) -> List[str]:
        data = self.get_json(
            {
                "action": "query",
                "list": "search",
                "srsearch": term,
                "srnamespace": "0",
                "srlimit": str(limit),
                "format": "json",
            }
        )
        titles = []
        for item in data.get("query", {}).get("search", []):
            title = item.get("title", "")
            if is_relevant_title(title):
                titles.append(title)
        return titles

    def page_extracts(self, titles: Iterable[str], batch_size: int = DEFAULT_BATCH_SIZE) -> List[Dict]:
        title_list = list(titles)
        records: List[Dict] = []
        for start in range(0, len(title_list), batch_size):
            batch = title_list[start : start + batch_size]
            data = self.get_json(
                {
                    "action": "query",
                    "prop": "extracts|pageprops",
                    "exintro": "1",
                    "explaintext": "1",
                    "redirects": "1",
                    "titles": "|".join(batch),
                    "format": "json",
                }
            )
            pages = data.get("query", {}).get("pages", {})
            for page in pages.values():
                records.append(page)
            time.sleep(SLEEP_SECONDS)
        return records


def is_relevant_title(title: str) -> bool:
    if not title:
        return False
    lower_title = title.lower()
    if any(lower_title.startswith(prefix) for prefix in EXCLUDED_TITLE_PREFIXES):
        return False
    if any(keyword in lower_title for keyword in EXCLUDED_TITLE_KEYWORDS):
        return False
    return True


def clean_summary(text: str, max_chars: int = 420) -> str:
    cleaned = re.sub(r"\s+", " ", text or "").strip()
    if not cleaned:
        return ""

    sentences = re.split(r"(?<=[.!?])\s+", cleaned)
    summary_parts: List[str] = []
    current_length = 0
    for sentence in sentences:
        if not sentence:
            continue
        if current_length + len(sentence) + 1 > max_chars and summary_parts:
            break
        summary_parts.append(sentence)
        current_length += len(sentence) + 1
        if len(summary_parts) >= 3:
            break

    summary = " ".join(summary_parts).strip()
    if len(summary) > max_chars:
        summary = summary[: max_chars - 3].rstrip() + "..."
    return summary


def matches_relevance(summary: str, relevance_terms: Iterable[str]) -> bool:
    for term in relevance_terms:
        pattern = r"\b" + re.escape(term) + r"\b"
        if re.search(pattern, summary):
            return True
    return False


def build_source_url(title: str) -> str:
    normalized = title.replace(" ", "_")
    return f"https://en.wikipedia.org/wiki/{urllib.parse.quote(normalized, safe='()')}"


def generate_domain_records(client: MediaWikiClient, spec: DomainSpec, max_depth: int) -> List[Dict]:
    candidate_titles: "OrderedDict[str, None]" = OrderedDict()

    for term in spec.search_terms:
        for title in client.search(term):
            candidate_titles.setdefault(title, None)

    pool_limit = spec.target_count * DEFAULT_TARGET_MULTIPLIER
    per_category_limit = max(160, spec.target_count)
    for category in spec.categories:
        for title in client.category_members(category, max_depth, per_category_limit):
            candidate_titles.setdefault(title, None)
            if len(candidate_titles) >= pool_limit:
                break
        if len(candidate_titles) >= pool_limit:
            break

    records = []
    for page in client.page_extracts(candidate_titles.keys()):
        if page.get("missing") is not None:
            continue
        if "disambiguation" in page.get("pageprops", {}):
            continue
        title = page.get("title", "").strip()
        if not is_relevant_title(title):
            continue
        summary = clean_summary(page.get("extract", ""))
        if len(summary) < 80:
            continue
        lower_summary = summary.lower()
        if not matches_relevance(lower_summary, spec.relevance_terms):
            continue
        records.append(
            {
                "id": f"{spec.code}:{page['pageid']}",
                "domain": spec.code,
                "title": title,
                "summary": summary,
                "sourceUrl": build_source_url(title),
                "sourceSite": "Wikipedia",
                "language": "en",
            }
        )
        if len(records) >= spec.target_count:
            break

    return records


def write_jsonl(path: Path, rows: Iterable[Dict]) -> int:
    count = 0
    with path.open("w", encoding="utf-8", newline="\n") as handle:
        for row in rows:
            handle.write(json.dumps(row, ensure_ascii=False) + "\n")
            count += 1
    return count


def count_jsonl_lines(path: Path) -> int:
    with path.open("r", encoding="utf-8") as handle:
        return sum(1 for _ in handle if _.strip())


def parse_args() -> argparse.Namespace:
    repo_root = Path(__file__).resolve().parents[1]
    default_output = repo_root / "qa-system-backend" / "src" / "main" / "resources" / "knowledge" / "general"

    parser = argparse.ArgumentParser(description="Build general knowledge seed JSONL files for the RAG pipeline.")
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=default_output,
        help=f"Directory for generated JSONL files. Default: {default_output}",
    )
    parser.add_argument(
        "--max-depth",
        type=int,
        default=DEFAULT_CATEGORY_DEPTH,
        help=f"Maximum category recursion depth. Default: {DEFAULT_CATEGORY_DEPTH}",
    )
    parser.add_argument(
        "--domains",
        nargs="*",
        default=[],
        help="Optional subset of domain codes to build.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    args.output_dir.mkdir(parents=True, exist_ok=True)

    selected_codes = {code.strip() for code in args.domains if code.strip()}
    domain_specs = [
        spec for spec in DOMAIN_SPECS
        if not selected_codes or spec.code in selected_codes
    ]
    if not domain_specs:
        print("No matching domains selected.", file=sys.stderr)
        return 1

    client = MediaWikiClient()
    for spec in domain_specs:
        print(f"[build] {spec.code} ...", flush=True)
        records = generate_domain_records(client, spec, args.max_depth)
        output_path = args.output_dir / f"{spec.code}.jsonl"
        count = write_jsonl(output_path, records)
        print(f"[done]  {spec.code}: {count} records -> {output_path}", flush=True)

    manifest = {
        "generatedAt": datetime.now(timezone.utc).isoformat(),
        "source": "en.wikipedia.org",
        "domains": {},
        "totalEntries": 0,
    }
    total_entries = 0
    for jsonl_file in sorted(args.output_dir.glob("*.jsonl")):
        count = count_jsonl_lines(jsonl_file)
        manifest["domains"][jsonl_file.stem] = {
            "count": count,
            "file": jsonl_file.name,
        }
        total_entries += count

    manifest["totalEntries"] = total_entries
    manifest_path = args.output_dir / "manifest.json"
    manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")

    print(f"[summary] total records: {total_entries}", flush=True)
    print(f"[summary] manifest: {manifest_path}", flush=True)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
