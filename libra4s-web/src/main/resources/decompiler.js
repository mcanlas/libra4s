document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("decompiler-form");
  const source = document.getElementById("source");
  const output = document.getElementById("output");

  if (!(form instanceof HTMLFormElement) || !(source instanceof HTMLTextAreaElement) || !(output instanceof HTMLDivElement)) {
    return;
  }

  form.addEventListener("submit", async event => {
    event.preventDefault();

    output.textContent = "Running...";

    try {
      const response = await fetch(form.action, {
        method: "POST",
        headers: {
          "Content-Type": "text/plain"
        },
        body: source.value
      });

      const text = await response.text();
      output.textContent = text;
    } catch (error) {
      output.textContent = error instanceof Error ? error.message : "Request failed";
    }
  });
});
