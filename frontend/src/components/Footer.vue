<template>
  <!-- Pie de página -->
  <footer class="footer">
    <div class="footer-content">
      <!-- Enlaces del footer -->
      <div class="footer-links">
        <a href="#" class="footer-link">Acerca de</a>
        <a href="#" class="footer-link">Recursos</a>
        <a href="#" class="footer-link">Legal</a>
      </div>
      <!-- Copyright -->
      <div class="copyright">
        © 2024 ecoBici
      </div>
    </div>
  </footer>
</template>

<script>
export default {
  name: 'Footer'
}
</script>

<style scoped>
.footer {
  background-color: #004E61; /* Azul oscuro de la marca */
  color: #FFFFFF;
  padding: 2rem 0;
  margin-top: auto; /* Empuja el footer al fondo cuando hay poco contenido */
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 2rem;
}

.footer-links {
  display: flex;
  gap: 2rem;
}

.footer-link {
  color: #FFFFFF;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-link:hover {
  color: #FBC02D; /* Color amarillo al hover */
}

.copyright {
  color: #FFFFFF;
}

/* Responsive: en móviles apilar verticalmente */
@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    gap: 1rem;
  }
}
</style>