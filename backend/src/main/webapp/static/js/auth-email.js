// Carry only the email within this tab; never store the password or put it in a URL.
(() => {
  const email = document.getElementById('email');
  const links = document.querySelectorAll('[data-create-account]');
  const key = 'tamo.signup.email';
  if (links.length) {
    links.forEach(link => link.addEventListener('click', () => {
      try { sessionStorage.setItem(key, email.value.trim()); } catch (_) { /* Storage may be disabled. */ }
    }));
  } else if (email) {
    try {
      const value = sessionStorage.getItem(key);
      sessionStorage.removeItem(key);
      if (value && !email.value) email.value = value;
    } catch (_) { /* Signup remains usable without browser storage. */ }
  }
})();
