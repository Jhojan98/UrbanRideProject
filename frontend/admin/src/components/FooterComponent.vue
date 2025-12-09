<template>
  <!-- Pie de página -->
  <footer class="footer">
    <div class="footer-content">
      <!-- Enlaces del footer -->
      <div class="footer-links">
        <a href="#" class="footer-link">{{ $t('footer.links.about') }}</a>
        <a href="#" class="footer-link">{{ $t('footer.links.resources') }}</a>
        <a href="#" class="footer-link">{{ $t('footer.links.legal') }}</a>
      </div>
      <!-- Copyright -->
      <div class="copyright">
        <i class="fa-brands fa-facebook"></i> Ecoride |
        <i class="fa-brands fa-instagram"></i> ecoride |
        <i class="fa-brands fa-tiktok"></i> ecoride |

        {{ $t('footer.copyright', { year: 2026, brand: 'ECORIDE' }) }}
      </div>
    </div>
  </footer>
</template>

<script setup>

</script>


<style lang="scss">
@import "@/styles/footer.scss";
</style>
