<template>
  <!-- Footer -->
  <footer class="footer">
    <div class="footer-content">
      <!-- Footer links -->
      <div class="footer-links">
        <a href="#" class="footer-link">{{ $t('footer.about') }}</a>
        <a href="#" class="footer-link">{{ $t('footer.resources') }}</a>
        <a href="#" class="footer-link">{{ $t('footer.legal') }}</a>
      </div>
      <!-- Copyright -->
      <div class="copyright">
        <i class="fa-brands fa-facebook"></i> Ecoride |
        <i class="fa-brands fa-instagram"></i> ecoride |
        <i class="fa-brands fa-tiktok"></i> ecoride |

        {{ $t('footer.copyright') }}
      </div>
    </div>
  </footer>
</template>

<script setup>
import { useI18n } from 'vue-i18n';
useI18n();
</script>


<style lang="scss">
@import "@/styles/footer.scss";
</style>
