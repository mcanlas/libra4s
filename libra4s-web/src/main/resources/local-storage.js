(() => {
  const sourceStorageKey = "libra4s.source";

  const readNonEmptyValue = key => {
    try {
      const value = window.localStorage.getItem(key);

      return typeof value === "string" && value.length > 0 ? value : null;
    } catch (_error) {
      return null;
    }
  };

  const writeValue = (key, value) => {
    try {
      window.localStorage.setItem(key, value);
    } catch (_error) {
      // Ignore storage failures so they do not break local exploration.
    }
  };

  window.libra4sLocalStorage = {
    readSavedSource: () => readNonEmptyValue(sourceStorageKey),
    saveSource: value => writeValue(sourceStorageKey, value)
  };
})();
