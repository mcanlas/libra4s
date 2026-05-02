document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("decompiler-form");
  const source = document.getElementById("source");
  const outputCompiler = document.getElementById("output-compiler");
  const outputDisassembly = document.getElementById("output-disassembly");

  if (!(form instanceof HTMLFormElement) || !(source instanceof HTMLTextAreaElement) || !(outputCompiler instanceof HTMLDivElement) || !(outputDisassembly instanceof HTMLDivElement)) {
    return;
  }

  const setOutput = text => {
    outputCompiler.textContent = text;
    outputDisassembly.textContent = text;
  };

  form.addEventListener("submit", async event => {
    event.preventDefault();

    setOutput("Running...");

    try {
      const response = await fetch("/compile", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ code: source.value })
      });

      if (!response.ok) {
        const text = await response.text();
        setOutput(text);
        return;
      }

      const payload = await response.json();
      outputCompiler.textContent = payload.compiler?.lines ?? "";
      outputDisassembly.textContent = Array.isArray(payload.javap?.lines) ? payload.javap.lines.join("\n") : "";
    } catch (error) {
      setOutput(error instanceof Error ? error.message : "Request failed");
    }
  });
});
