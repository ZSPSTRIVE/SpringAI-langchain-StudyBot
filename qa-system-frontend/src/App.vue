<template>
  <router-view />
  <AiFloatingWidget />
</template>

<script setup>
import AiFloatingWidget from '@/components/common/AiFloatingWidget.vue'
</script>

<style lang="scss">
// 全局字体样式
* {
  box-sizing: border-box;
}

html, body {
  font-family: $font-family;
  color: $text-primary;
  background: $bg-primary;
}

// 标题样式
h1, h2, h3, h4, h5, h6 {
  font-weight: $font-weight-semibold;
  line-height: $line-height-tight;
  letter-spacing: $letter-spacing-tight;
}

h1 { font-size: $font-size-3xl; }
h2 { font-size: $font-size-2xl; }
h3 { font-size: $font-size-xl; }
h4 { font-size: $font-size-lg; }
h5 { font-size: $font-size-md; }
h6 { font-size: $font-size-sm; }

// 正文样式
p, span, div {
  line-height: $line-height-normal;
}

// 小字体优化
small, .text-small {
  font-size: $font-size-xs;
  letter-spacing: $letter-spacing-wide;
}

// 代码字体
code, pre, .mono {
  font-family: $font-family-mono;
}

// 链接样式
a {
  color: $primary-color;
  text-decoration: none;
  
  &:hover {
    text-decoration: underline;
  }
}

// Element Plus 字体覆盖
.el-button, .el-input, .el-select, .el-dialog,
.el-message, .el-notification, .el-form-item__label,
.el-menu, .el-tabs, .el-tag, .el-badge {
  font-family: $font-family !important;
}

// 滚动条美化
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.15);
  border-radius: 3px;
  
  &:hover {
    background: rgba(0, 0, 0, 0.25);
  }
}
</style>


