document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("decompiler-form");
  const source = document.getElementById("source");
  const outputCompiler = document.getElementById("output-compiler");
  const outputDisassembly = document.getElementById("output-disassembly");

  if (!(form instanceof HTMLFormElement) || !(source instanceof HTMLTextAreaElement) || !(outputCompiler instanceof HTMLDivElement) || !(outputDisassembly instanceof HTMLDivElement)) {
    return;
  }

  const formatLines = lines => Array.isArray(lines) ? lines.join("\n") : "";

  const escapeHtml = text => String(text)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");

  const formatLineWithCommentHighlight = line => {
    const i = line.indexOf("//");

    if (i < 0) {
      return escapeHtml(line);
    }

    const code = escapeHtml(line.slice(0, i));
    const comment = escapeHtml(line.slice(i));
    return `${code}<span class="javap-comment">${comment}</span>`;
  };

  const formatCompilerLineWithPhaseHighlight = line => {
    if (line.startsWith("[[") && line.includes("]]")) {
      return `<span class="compiler-phase-header">${escapeHtml(line)}</span>`;
    }

    return escapeHtml(line);
  };

  const formatCompilerLinesHtml = linesText => String(linesText ?? "")
    .split("\n")
    .map(formatCompilerLineWithPhaseHighlight)
    .join("\n");

  const formatJavapOutputsHtml = outputs => {
    if (!Array.isArray(outputs)) {
      return "";
    }

    return outputs
      .map(output => {
        const classFile = typeof output?.classFile === "string" ? output.classFile : "(unknown class)";
        const header = escapeHtml(classFile);
        const lines = Array.isArray(output?.lines)
          ? output.lines.map(formatLineWithCommentHighlight).join("\n")
          : "";

        return lines ? `${header}\n${lines}` : header;
      })
      .join("\n\n");
  };

  const formatFailureHtml = error => {
    if (Array.isArray(error?.errors)) {
      return error.errors
        .map(item => {
          const exitCode = typeof item?.exitCode === "number" ? `exit ${item.exitCode}` : "exit ?";
          const lines = formatLines(item?.lines);
          return lines
            ? `${escapeHtml(exitCode)}\n${escapeHtml(lines)}`
            : escapeHtml(exitCode);
        })
        .join("\n\n");
    }

    const exitCode = typeof error?.exitCode === "number" ? `exit ${error.exitCode}` : "exit ?";
    const lines = formatLines(error?.lines);
    return lines ? `${escapeHtml(exitCode)}\n${escapeHtml(lines)}` : escapeHtml(exitCode);
  };

  const formatAttemptHtml = (attempt, selectSuccessHtml) => {
    if (!attempt || typeof attempt !== "object") {
      return "";
    }

    if (attempt.state === "success") {
      return selectSuccessHtml(attempt.value ?? {});
    }

    if (attempt.state === "failure") {
      return formatFailureHtml(attempt.error ?? {});
    }

    return "";
  };

  const setOutput = text => {
    const escaped = escapeHtml(text);
    outputCompiler.innerHTML = escaped;
    outputDisassembly.innerHTML = escaped;
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
        outputCompiler.innerHTML = formatAttemptHtml(data.compiler, compiler => formatCompilerLinesHtml(compiler.lines));
        outputDisassembly.innerHTML = formatAttemptHtml(data.javap, javap => formatJavapOutputsHtml(javap.outputs));
      } else {
        const error = payload?.error;
        setOutput(typeof error === "string" ? error : JSON.stringify(error ?? "Request failed", null, 2));
      }
    } catch (error) {
      setOutput(error instanceof Error ? error.message : "Request failed");
    }
  });
});
