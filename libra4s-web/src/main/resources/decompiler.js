document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("decompiler-form");
  const submit = document.getElementById("submit");
  const source = document.getElementById("source");
  const compilerStageIcon = document.getElementById("compiler-stage-icon");
  const disassemblyStageIcon = document.getElementById("disassembly-stage-icon");
  const outputCompiler = document.getElementById("output-compiler");
  const outputDisassembly = document.getElementById("output-disassembly");
  const runIndicator = document.getElementById("run-indicator");
  const runIndicatorIcon = document.getElementById("run-indicator-icon");
  const runIndicatorText = document.getElementById("run-indicator-text");

  if (
    !(form instanceof HTMLFormElement) ||
    !(submit instanceof HTMLButtonElement) ||
    !(source instanceof HTMLTextAreaElement) ||
    !(compilerStageIcon instanceof HTMLSpanElement) ||
    !(disassemblyStageIcon instanceof HTMLSpanElement) ||
    !(outputCompiler instanceof HTMLDivElement) ||
    !(outputDisassembly instanceof HTMLDivElement) ||
    !(runIndicator instanceof HTMLSpanElement) ||
    !(runIndicatorIcon instanceof HTMLSpanElement) ||
    !(runIndicatorText instanceof HTMLSpanElement)
  ) {
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

  const expandedCompilerPhaseKeys = new Map();

  const stablePhaseKey = (phase, index, phaseCounts) => {
    const hint = typeof phase?.hint === "string" && phase.hint.length > 0
      ? phase.hint
      : `phase-${index + 1}`;
    const count = (phaseCounts.get(hint) ?? 0) + 1;

    phaseCounts.set(hint, count);

    return count === 1 ? hint : `${hint} #${count}`;
  };

  const formatCompilerPhasesHtml = phases => {
    if (!Array.isArray(phases) || phases.length === 0) {
      return "";
    }

    const phaseCounts = new Map();

    return `<div class="compiler-phases">${phases
      .map((phase, index) => {
        const key = stablePhaseKey(phase, index, phaseCounts);
        const hint = typeof phase?.hint === "string" && phase.hint.length > 0
          ? phase.hint
          : `Phase ${index + 1}`;
        const lines = Array.isArray(phase?.lines) ? phase.lines.join("\n") : "";
        const open = expandedCompilerPhaseKeys.has(key)
          ? expandedCompilerPhaseKeys.get(key) === true
          : index === 0;

        return `<details class="compiler-phase" data-phase-key="${escapeHtml(key)}"${open ? " open" : ""}>
  <summary class="compiler-phase-summary">${escapeHtml(hint)}</summary>
  <pre class="compiler-phase-lines">${escapeHtml(lines)}</pre>
</details>`;
      })
      .join("")}</div>`;
  };

  const formatCompilerSuccessHtml = compiler => {
    const phasesHtml = formatCompilerPhasesHtml(compiler.phases);

    return phasesHtml || formatCompilerLinesHtml(compiler.lines);
  };

  const hasCompilerPhases = compiler =>
    Array.isArray(compiler?.value?.phases) && compiler.value.phases.length > 0;

  const stageIcons = {
    // no icon for idle state
    idle: { icon: "", label: "" },

    // U+23F3 hourglass not done
    running: { icon: "\u23f3", label: "Running" },

    // U+2705 white heavy check mark
    success: { icon: "\u2705", label: "Succeeded" },

    // U+274C cross mark
    failure: { icon: "\u274c", label: "Failed" },

    // U+23ED + VS16 next track button
    skipped: { icon: "\u23ed\ufe0f", label: "Skipped" }
  };

  const setStageIcon = (element, status) => {
    const icon = stageIcons[status] ?? stageIcons.idle;

    element.textContent = icon.icon;
    element.title = icon.label;
    element.setAttribute("aria-label", icon.label);
  };

  const setStageIcons = (compilerStatus, disassemblyStatus) => {
    setStageIcon(compilerStageIcon, compilerStatus);
    setStageIcon(disassemblyStageIcon, disassemblyStatus);
  };

  const runIndicatorStates = {
    idle: { icon: "", label: "" },
    running: stageIcons.running,
    done: { icon: stageIcons.success.icon, label: "Done" }
  };

  let latestRequestId = 0;

  const setRunningIndicator = state => {
    const indicator = runIndicatorStates[state] ?? runIndicatorStates.idle;
    const isIdle = state === "idle";

    runIndicator.hidden = isIdle;
    runIndicatorIcon.textContent = indicator.icon;
    runIndicatorText.textContent = indicator.label;
    runIndicator.setAttribute("aria-label", indicator.label);
  };

  const isLatestRequest = requestId => requestId === latestRequestId;

  const processErrorLines = error => {
    if (Array.isArray(error?.errors)) {
      return error.errors.flatMap(item => Array.isArray(item?.lines) ? item.lines : []);
    }

    return Array.isArray(error?.lines) ? error.lines : [];
  };

  const isSkippedJavapAttempt = attempt => {
    if (attempt?.state !== "failure") {
      return false;
    }

    return processErrorLines(attempt.error)
      .some(line => line === "Compilation failed" || line === "No class files generated");
  };

  const stageStatus = (attempt, skipped) => {
    if (skipped === true) {
      return "skipped";
    }

    if (attempt?.state === "success") {
      return "success";
    }

    if (attempt?.state === "failure") {
      return "failure";
    }

    return "idle";
  };

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

    outputCompiler.classList.remove("has-structured-output");
    outputDisassembly.classList.remove("has-structured-output");
    outputCompiler.innerHTML = escaped;
    outputDisassembly.innerHTML = escaped;
  };

  outputCompiler.addEventListener("toggle", event => {
    const target = event.target;

    if (target instanceof HTMLDetailsElement && target.classList.contains("compiler-phase")) {
      const key = target.dataset.phaseKey;

      if (typeof key === "string") {
        expandedCompilerPhaseKeys.set(key, target.open);
      }
    }
  }, true);

  form.addEventListener("submit", async event => {
    if (event.submitter !== submit) {
      return;
    }

    event.preventDefault();

    const requestId = ++latestRequestId;

    setRunningIndicator("running");
    setStageIcons("running", "running");
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

      if (!isLatestRequest(requestId)) {
        return;
      }

      if (payload?.ok === true) {
        const data = payload.data ?? {};

        outputCompiler.innerHTML = formatAttemptHtml(data.compiler, formatCompilerSuccessHtml);
        outputCompiler.classList.toggle("has-structured-output", hasCompilerPhases(data.compiler));
        outputDisassembly.innerHTML = formatAttemptHtml(data.javap, javap => formatJavapOutputsHtml(javap.outputs));
        outputDisassembly.classList.remove("has-structured-output");
        setStageIcons(
          stageStatus(data.compiler, false),
          stageStatus(data.javap, isSkippedJavapAttempt(data.javap))
        );
      } else {
        const error = payload?.error;

        setOutput(typeof error === "string" ? error : JSON.stringify(error ?? "Request failed", null, 2));
        setStageIcons("failure", "failure");
      }
    } catch (error) {
      if (!isLatestRequest(requestId)) {
        return;
      }

      setOutput(error instanceof Error ? error.message : "Request failed");
      setStageIcons("failure", "failure");
    } finally {
      if (isLatestRequest(requestId)) {
        setRunningIndicator("done");
      }
    }
  });
});
