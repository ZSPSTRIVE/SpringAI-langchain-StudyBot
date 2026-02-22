param(
    [switch]$SkipTests = $false
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Push-Location $repoRoot

try {
    function Invoke-RgCheck {
        param(
            [string]$Pattern,
            [string]$ErrorMessage,
            [string[]]$Paths = @("src/main/java")
        )

        & rg -n --hidden --glob "!.git/**" --glob "!target/**" --glob "!*.md" $Pattern @Paths | Out-Null
        $exitCode = $LASTEXITCODE

        if ($exitCode -eq 0) {
            $matches = & rg -n --hidden --glob "!.git/**" --glob "!target/**" --glob "!*.md" $Pattern @Paths
            throw "$ErrorMessage`n$matches"
        }
        if ($exitCode -gt 1) {
            throw "ripgrep execution failed. pattern=$Pattern"
        }
    }

    Write-Host "[review] 1/5 compile check"
    mvn -DskipTests compile

    if (-not $SkipTests) {
        Write-Host "[review] 2/5 unit test check"
        mvn test
    } else {
        Write-Host "[review] 2/5 unit test check skipped"
    }

    Write-Host "[review] 3/5 hardcoded secret scan"
    Invoke-RgCheck `
        -Pattern "sk-[A-Za-z0-9]{20,}|AKID[0-9A-Za-z]{16}|secret(Id|Key)\s*[:=]\s*[A-Za-z0-9_./+\-]{16,}" `
        -ErrorMessage "Potential hardcoded secret detected:" `
        -Paths @("src/main/java", "src/main/resources")

    Write-Host "[review] 4/5 high-risk API scan"
    Invoke-RgCheck `
        -Pattern "Runtime\.getRuntime\(\)\.exec|ProcessBuilder\(|ScriptEngineManager\(" `
        -ErrorMessage "Potential high-risk API usage detected:" `
        -Paths @("src/main/java")

    Write-Host "[review] 5/5 SQL injection spot-check"
    Invoke-RgCheck `
        -Pattern "(SELECT|UPDATE|INSERT|DELETE).*[+].*" `
        -ErrorMessage "Potential SQL concatenation detected:" `
        -Paths @("src/main/java")

    Write-Host "[review] PASS"
}
finally {
    Pop-Location
}
