<script setup lang="ts">
import { RouterView } from 'vue-router'
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
  /* Previously led with "Inter", which was never loaded — every visitor silently
     fell through to the system font anyway. Naming the system stack directly
     costs zero requests and renders with no swap flash. */
  font-family:
    -apple-system,
    BlinkMacSystemFont,
    'Segoe UI',
    Roboto,
    'Helvetica Neue',
    Arial,
    sans-serif;
  color: #1a1a1a;
  background-color: #f8f9fa;
  -webkit-font-smoothing: antialiased;
}

#app {
  min-height: 100vh;
}
</style>
