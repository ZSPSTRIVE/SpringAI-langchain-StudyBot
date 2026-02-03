module.exports = {
  root: true,
  env: {
    browser: true,
    node: true,
    es2022: true,
  },
  parser: 'vue-eslint-parser',
  parserOptions: {
    ecmaVersion: 'latest',
    sourceType: 'module',
  },
  extends: ['eslint:recommended', 'plugin:vue/vue3-recommended'],
  rules: {
    'no-unused-vars': 'off',
    'no-empty': 'off',
    'no-constant-condition': 'off',
    'no-case-declarations': 'off',
    'vue/multi-word-component-names': 'off',
    'vue/no-v-html': 'off',
  },
}
