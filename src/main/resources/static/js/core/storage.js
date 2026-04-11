(function () {
  window.CC = window.CC || {};

  const storage = {
    get(key) {
      return localStorage.getItem(key);
    },
    set(key, value) {
      localStorage.setItem(key, value);
    },
    remove(key) {
      localStorage.removeItem(key);
    }
  };

  window.CC.storage = storage;
})();
