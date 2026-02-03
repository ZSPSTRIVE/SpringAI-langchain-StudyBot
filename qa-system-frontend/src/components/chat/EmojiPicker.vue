<template>
  <div class="emoji-picker">
    <!-- 表情包标签 -->
    <div class="emoji-tabs">
      <div
        v-for="pack in emojiPacks"
        :key="pack.id"
        :class="['emoji-tab', { active: activePack === pack.id }]"
        @click="activePack = pack.id"
      >
        {{ pack.name }}
      </div>
    </div>

    <!-- 表情列表 -->
    <div class="emoji-grid">
      <!-- 抖音热门表情 -->
      <template v-if="activePack === 1">
        <div
          v-for="emoji in douyinEmojis"
          :key="emoji.code"
          class="emoji-item"
          :title="emoji.name"
          @click="selectEmoji(emoji)"
        >
          <span class="emoji-text">{{ emoji.text }}</span>
        </div>
      </template>

      <!-- 经典表情 -->
      <template v-else-if="activePack === 2">
        <div
          v-for="emoji in classicEmojis"
          :key="emoji.code"
          class="emoji-item"
          :title="emoji.name"
          @click="selectEmoji(emoji)"
        >
          <span class="emoji-unicode">{{ emoji.unicode }}</span>
        </div>
      </template>

      <!-- 颜文字 -->
      <template v-else-if="activePack === 3">
        <div
          v-for="emoji in kaomojiEmojis"
          :key="emoji.code"
          class="emoji-item kaomoji"
          :title="emoji.name"
          @click="selectEmoji(emoji)"
        >
          {{ emoji.text }}
        </div>
      </template>
    </div>

    <!-- 最近使用 -->
    <div
      v-if="recentEmojis.length > 0"
      class="recent-section"
    >
      <div class="recent-title">
        最近使用
      </div>
      <div class="recent-grid">
        <div
          v-for="emoji in recentEmojis"
          :key="emoji.code"
          class="emoji-item small"
          @click="selectEmoji(emoji)"
        >
          {{ emoji.unicode || emoji.text }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const emit = defineEmits(['select'])

const activePack = ref(1)
const recentEmojis = ref([])

// 表情包列表
const emojiPacks = [
  { id: 1, name: '抖音热门' },
  { id: 2, name: '经典表情' },
  { id: 3, name: '颜文字' }
]

// 抖音风格热门表情（文字表情）
const douyinEmojis = [
  { code: '[无语]', name: '无语', text: '😑' },
  { code: '[裂开]', name: '裂开', text: '😭' },
  { code: '[社死]', name: '社死', text: '😱' },
  { code: '[绝绝子]', name: '绝绝子', text: '🤯' },
  { code: '[YYDS]', name: 'YYDS', text: '🏆' },
  { code: '[芭比Q]', name: '芭比Q了', text: '🔥' },
  { code: '[破防]', name: '破防了', text: '💔' },
  { code: '[不理解]', name: '不理解', text: '🤔' },
  { code: '[笑哭]', name: '笑哭', text: '😂' },
  { code: '[狗头]', name: '狗头', text: '🐶' },
  { code: '[doge]', name: 'doge', text: '🐕' },
  { code: '[捂脸]', name: '捂脸', text: '🤦' },
  { code: '[奸笑]', name: '奸笑', text: '😏' },
  { code: '[墨镜]', name: '墨镜', text: '😎' },
  { code: '[苦涩]', name: '苦涩', text: '😣' },
  { code: '[翻白眼]', name: '翻白眼', text: '🙄' },
  { code: '[打call]', name: '打call', text: '📣' },
  { code: '[respect]', name: 'respect', text: '🫡' },
  { code: '[加油]', name: '加油', text: '💪' },
  { code: '[666]', name: '666', text: '👍' },
  { code: '[爱了]', name: '爱了', text: '🥰' },
  { code: '[心动]', name: '心动', text: '💓' },
  { code: '[暴富]', name: '暴富', text: '💰' },
  { code: '[吃瓜]', name: '吃瓜', text: '🍉' }
]

// 经典 Unicode 表情
const classicEmojis = [
  { code: '[微笑]', name: '微笑', unicode: '😊' },
  { code: '[大笑]', name: '大笑', unicode: '😄' },
  { code: '[偷笑]', name: '偷笑', unicode: '🤭' },
  { code: '[可爱]', name: '可爱', unicode: '🥺' },
  { code: '[色]', name: '色', unicode: '😍' },
  { code: '[亲亲]', name: '亲亲', unicode: '😘' },
  { code: '[吐舌]', name: '吐舌', unicode: '😜' },
  { code: '[害羞]', name: '害羞', unicode: '😳' },
  { code: '[闭嘴]', name: '闭嘴', unicode: '🤐' },
  { code: '[睡]', name: '睡', unicode: '😴' },
  { code: '[大哭]', name: '大哭', unicode: '😭' },
  { code: '[尴尬]', name: '尴尬', unicode: '😅' },
  { code: '[发怒]', name: '发怒', unicode: '😠' },
  { code: '[调皮]', name: '调皮', unicode: '😝' },
  { code: '[惊讶]', name: '惊讶', unicode: '😲' },
  { code: '[难过]', name: '难过', unicode: '😢' },
  { code: '[抓狂]', name: '抓狂', unicode: '😤' },
  { code: '[吐]', name: '吐', unicode: '🤮' },
  { code: '[思考]', name: '思考', unicode: '🤔' },
  { code: '[晕]', name: '晕', unicode: '😵' },
  { code: '[奋斗]', name: '奋斗', unicode: '💪' },
  { code: '[疑问]', name: '疑问', unicode: '❓' },
  { code: '[嘘]', name: '嘘', unicode: '🤫' },
  { code: '[晕]', name: '晕', unicode: '😵‍💫' },
  { code: '[衰]', name: '衰', unicode: '😩' },
  { code: '[骷髅]', name: '骷髅', unicode: '💀' },
  { code: '[敲打]', name: '敲打', unicode: '🔨' },
  { code: '[再见]', name: '再见', unicode: '👋' },
  { code: '[抠鼻]', name: '抠鼻', unicode: '🤏' },
  { code: '[鼓掌]', name: '鼓掌', unicode: '👏' },
  { code: '[糗大了]', name: '糗大了', unicode: '😰' },
  { code: '[坏笑]', name: '坏笑', unicode: '😈' },
  { code: '[左哼哼]', name: '左哼哼', unicode: '😤' },
  { code: '[右哼哼]', name: '右哼哼', unicode: '😤' },
  { code: '[哈欠]', name: '哈欠', unicode: '🥱' },
  { code: '[委屈]', name: '委屈', unicode: '🥺' },
  { code: '[快哭了]', name: '快哭了', unicode: '😿' },
  { code: '[阴险]', name: '阴险', unicode: '😼' },
  { code: '[爱心]', name: '爱心', unicode: '❤️' },
  { code: '[心碎]', name: '心碎', unicode: '💔' },
  { code: '[玫瑰]', name: '玫瑰', unicode: '🌹' },
  { code: '[凋谢]', name: '凋谢', unicode: '🥀' },
  { code: '[嘴唇]', name: '嘴唇', unicode: '💋' },
  { code: '[礼物]', name: '礼物', unicode: '🎁' },
  { code: '[太阳]', name: '太阳', unicode: '☀️' },
  { code: '[月亮]', name: '月亮', unicode: '🌙' },
  { code: '[星星]', name: '星星', unicode: '⭐' },
  { code: '[闪电]', name: '闪电', unicode: '⚡' }
]

// 颜文字表情
const kaomojiEmojis = [
  { code: '[开心]', name: '开心', text: '(◕‿◕)' },
  { code: '[卖萌]', name: '卖萌', text: '(●\'◡\'●)' },
  { code: '[无奈]', name: '无奈', text: '╮(╯▽╰)╭' },
  { code: '[大哭]', name: '大哭', text: '(╥﹏╥)' },
  { code: '[惊讶]', name: '惊讶', text: 'Σ(°△°|||)' },
  { code: '[生气]', name: '生气', text: '(╬▔皿▔)╯' },
  { code: '[害羞]', name: '害羞', text: '(*/ω＼*)' },
  { code: '[委屈]', name: '委屈', text: '(｡•́︿•̀｡)' },
  { code: '[思考]', name: '思考', text: '(￣.￣)' },
  { code: '[睡觉]', name: '睡觉', text: '(￣o￣) . z Z' },
  { code: '[酷]', name: '酷', text: '(▀̿Ĺ̯▀̿ ̿)' },
  { code: '[比心]', name: '比心', text: '♡(ӦｖӦ｡)' },
  { code: '[加油]', name: '加油', text: '٩(๑❛ᴗ❛๑)۶' },
  { code: '[击掌]', name: '击掌', text: '( •̀ ω •́ )✧' },
  { code: '[无语]', name: '无语', text: '(-_-) zzZ' },
  { code: '[摊手]', name: '摊手', text: '¯\\_(ツ)_/¯' },
  { code: '[疑惑]', name: '疑惑', text: '(・・?)' },
  { code: '[偷笑]', name: '偷笑', text: '(￣▽￣)"' },
  { code: '[翻白眼]', name: '翻白眼', text: '(¬_¬)' },
  { code: '[期待]', name: '期待', text: '(✧ω✧)' }
]

const selectEmoji = (emoji) => {
  emit('select', emoji)
  addToRecent(emoji)
}

const addToRecent = (emoji) => {
  // 移除已存在的
  const index = recentEmojis.value.findIndex(e => e.code === emoji.code)
  if (index > -1) {
    recentEmojis.value.splice(index, 1)
  }
  // 添加到开头
  recentEmojis.value.unshift(emoji)
  // 最多保留 10 个
  if (recentEmojis.value.length > 10) {
    recentEmojis.value.pop()
  }
  // 保存到本地
  localStorage.setItem('recentEmojis', JSON.stringify(recentEmojis.value))
}

onMounted(() => {
  // 从本地读取最近使用
  const saved = localStorage.getItem('recentEmojis')
  if (saved) {
    try {
      recentEmojis.value = JSON.parse(saved)
    } catch (e) {
      console.error('解析最近表情失败:', e)
    }
  }
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.emoji-picker {
  width: 100%;
}

.emoji-tabs {
  display: flex;
  border-bottom: 2px solid $neo-black;
  margin-bottom: $spacing-sm;
  
  .emoji-tab {
    flex: 1;
    padding: $spacing-xs $spacing-sm;
    text-align: center;
    cursor: pointer;
    font-size: 12px;
    font-weight: 600;
    transition: all 150ms;
    
    &:hover {
      background: rgba($neo-black, 0.05);
    }
    
    &.active {
      background: $neo-yellow;
      color: $neo-black;
    }
  }
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
  max-height: 200px;
  overflow-y: auto;
  padding: $spacing-xs;
}

.emoji-item {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 150ms;
  
  &:hover {
    background: $neo-yellow;
    transform: scale(1.1);
  }
  
  .emoji-text,
  .emoji-unicode {
    font-size: 24px;
  }
  
  &.kaomoji {
    width: auto;
    padding: 4px 8px;
    font-size: 12px;
    grid-column: span 2;
  }
  
  &.small {
    width: 32px;
    height: 32px;
    
    .emoji-text,
    .emoji-unicode {
      font-size: 18px;
    }
  }
}

.recent-section {
  border-top: 1px solid rgba($neo-black, 0.1);
  margin-top: $spacing-sm;
  padding-top: $spacing-sm;
  
  .recent-title {
    font-size: 12px;
    color: $text-secondary;
    margin-bottom: $spacing-xs;
  }
  
  .recent-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
  }
}
</style>
