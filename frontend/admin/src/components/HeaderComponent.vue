.header {
  background-color: var(--color-primary-light);
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  padding: 0.5rem 0; /* Reducido de 1rem */
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 56px; /* Altura fija */
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 1.5rem; /* Reducido de 2rem */
  height: 100%;
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.logo-img {
  height: 32px; /* Reducido de 40px */
  width: auto;
  object-fit: cover;
  border-radius: 50%;
}

.logo-text {
  font-size: 1.2rem; /* Reducido de 1.5rem */
  font-weight: bold;
  color: #FFFFFF;
  text-transform: uppercase; /* Para consistencia con el diseño */
  letter-spacing: 0.5px;
}

.nav {
  display: flex;
  gap: 1.5rem; /* Reducido de 2rem */
  align-items: center;
}

.nav-link {
  text-decoration: none;
  color: var(--color-background-light);
  font-weight: 500;
  font-size: 0.9rem; /* Tamaño consistente */
  transition: color 0.3s;
  text-transform: none; /* Sin mayúsculas forzadas */
  letter-spacing: normal;
  padding: 0.25rem 0;
}

.nav-link:hover {
  color: var(--color-surface-light);
  transform: translateY(-1px); /* Animación más sutil */
}

.nav-link.router-link-active {
  color: var(--color-accent-light);
  font-weight: 600;
  border-bottom: 2px solid var(--color-accent-light);
}

.auth-buttons {
  display: flex;
  gap: 0.75rem; /* Reducido de 1rem */
  align-items: center;
}

.lang-switch, .logout-btn {
  padding: 0.4rem 0.8rem; /* Más compactos */
  font-size: 0.85rem;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.1);
  color: white;
  text-transform: none; /* Sin mayúsculas */
  font-weight: 500;
  transition: all 0.2s;
}

.lang-switch:hover, .logout-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 0.75rem;
    padding: 0.5rem;
    height: auto;
  }
  
  .nav {
    gap: 0.75rem;
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .nav-link {
    font-size: 0.8rem;
  }
  
  .auth-buttons {
    gap: 0.5rem;
  }
}