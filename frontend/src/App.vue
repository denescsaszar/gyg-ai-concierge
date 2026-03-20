<script setup lang="ts">
import { RouterView } from "vue-router";
</script>

<template>
  <div id="app">
    <RouterView />
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family:
    "Inter",
    -apple-system,
    BlinkMacSystemFont,
    sans-serif;
  color: #1a1a1a;
  background-color: #f8f9fa;
}

#app {
  min-height: 100vh;
}
</style>
