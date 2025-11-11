<template>
  <div class="blank-layout">
    <slot />
  </div>
</template>

<script setup lang="ts">
// Simple layout without header/footer (login/signup pages, etc.)
</script>

<style scoped>
.blank-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
</style>
