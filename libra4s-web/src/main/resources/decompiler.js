document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("decompiler-form");
  const source = document.getElementById("source");
  const outputCompiler = document.getElementById("output-compiler");
  const outputDisassembly = document.getElementById("output-disassembly");

  if (!(form instanceof HTMLFormElement) || !(source instanceof HTMLTextAreaElement) || !(outputCompiler instanceof HTMLDivElement) || !(outputDisassembly instanceof HTMLDivElement)) {
    return;
  }

  const formatLines = lines => Array.isArray(lines) ? lines.join("\n") : "";

  const formatAttempt = (attempt, selectSuccessLines) => {
    if (!attempt || typeof attempt !== "object") {
      return "";
    }

    if (attempt.state === "success") {
      return selectSuccessLines(attempt.value ?? {});
    }

    if (attempt.state === "failure") {
      const error = attempt.error ?? {};
      const exitCode = typeof error.exitCode === "number" ? `exit ${error.exitCode}` : "exit ?";
      const lines = formatLines(error.lines);
      return lines ? `${exitCode}\n${lines}` : exitCode;
    }


    return "";
  };

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

      const payload = await response.json();

      if (payload?.ok === true) {
        const data = payload.data ?? {};
        outputCompiler.textContent = formatAttempt(data.compiler, compiler => compiler.lines ?? "");
        outputDisassembly.textContent = formatAttempt(data.javap, javap => formatLines(javap.lines));
      } else {
        const error = payload?.error;
        setOutput(typeof error === "string" ? error : JSON.stringify(error ?? "Request failed", null, 2));
      }
    } catch (error) {
      setOutput(error instanceof Error ? error.message : "Request failed");
    }
  });
});
