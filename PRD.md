# libra4s PRD

## Status

Drafted from repository inspection on 2026-05-02. This document should be updated as the product goals become clearer through interviews.
This PRD uses roadmap phases (near term, medium term, long term) rather than formal version labels.

## Product Summary

libra4s is a web-based Scala generated-code exploration tool. The current implementation lets a user paste Scala source, run it through `scalac -Vprint:all`, and inspect both compiler phase output and `javap` disassembly for the generated class file.

## Confirmed Current Behavior

- The root README describes the project as "Exploring Scala compilation."
- The web app serves a single page at `/`.
- The UI has a Scala source textarea, a `Run` button, and two output columns:
  - Compiler Output
  - Disassembly Output
- The browser submits JSON to `POST /compile` with a `code` field.
- The server writes the submitted code to a temporary `Input.scala`.
- The compiler command is `scalac -Vprint:all -color:never -d <tempDir> <scalaFile>`.
- Compiler output is parsed into phase sections when lines begin with `[[syntax trees at end of`.
- The server finds the first `.class` file generated in the temporary directory.
- The disassembler command is currently `javap <classFile>`.
- The response contains:
  - `compiler.lines`: a full textual compiler output dump
  - `compiler.phases`: structured phase output
  - `javap.lines`: disassembler output lines

## Working Product Assumptions

- Primary audience: Scala developers who want to understand what code is generated from a small Scala input.
- Primary job: quickly move from a Scala snippet to generated compiler output and JVM class/disassembly output without setting up local commands manually.
- Initial scope should favor learning and inspection over production-grade remote compilation.
- The web UI should make generated compiler phases and disassembly easier to navigate than raw terminal output.
- The tool should keep examples small and fast, with clear errors when compilation fails.

## Goals

- Make generated-code exploration from Scala snippets approachable in a browser.
- Preserve access to raw output while adding structure that helps scanning.
- Help users connect source-level Scala code to generated JVM artifacts.
- Keep the local developer loop simple: sbt project, http4s service, minimal state.

## Non-Goals

- Support arbitrary multi-file projects in the first product shape.
- Provide a hosted public sandbox without explicit security hardening.
- Replace IDE/compiler diagnostics.
- Support every scalac or javap option before the core learning workflow is clear.

## User Experience Requirements

- Users can paste Scala source and run compilation from the first screen.
- Users can inspect compiler output and disassembly side by side.
- The left output pane should remain focused on compiler phases.
- The right output pane should remain focused on `javap` output.
- The default right-pane `javap` view should use no optional disassembler checkboxes.
- Compiler phase output should be broken into collapsible sections with one header per phase.
- Compiler option checkboxes are a research task; no compiler checkboxes are currently known.
- Disassembler option checkboxes are also a research task.
- Error states should distinguish invalid requests, compilation failures, and server/tooling failures.
- Compile failures should still return captured attempt details, including command output and enough structured context to explain what failed.
- On compile failure, the UI should render the compiler attempt error channel and mark the disassembler output as skipped when no class file exists.
- Async submissions should eventually show status lights that communicate request validity or invalidity.
- Async status indicators should use emoji-style icons:
  - idle: no icon for now
  - done: green checkbox
  - invalid/error: red X
- Compiler and disassembler stage icons should appear in their respective pane headers.
- The UI should have a separate running indicator outside the two destination output panes so existing results can remain visible while a new request runs.
- Async submission should support debounce-on-edit auto-submission, with an explicit `Run` action remaining available for manual reruns.
- The debounce delay should be 300ms.
- Autosubmit should not cancel in-flight work; stale responses should be ignored by request identity.
- For repeated overlapping submissions, the UI should use a last-request-wins strategy.
- Source text should persist across reloads via local storage.
- Restoring source from local storage should not trigger autosubmit until the user edits the snippet.
- Future compiler and `javap` option selections should also persist via local storage once those option controls exist.
- The page should remain usable for long output.

## Technical Requirements

- Continue using Scala 3, Cats Effect, http4s, Circe, and Scalatags unless a concrete product requirement pushes otherwise.
- Keep core command-running behavior in `libra4s-core`.
- Keep HTTP/UI concerns in `libra4s-web`.
- IO command runs should be enriched into attempt values with an explicit error channel instead of only succeeding with output or failing the effect.
- Attempt error channels should be exposed through the API so the UI can render them directly.
- Attempt errors should start as `ProcessError(exitCode: Int, lines: List[String])`.
- Stage attempts should be modeled as `Either[ProcessError, A]`, where `A` is the successful stage output.
- UI status should generally be derived from attempt shape and stage presence rather than duplicated as an independent source of truth in the core attempt model.
- Requests should eventually have deterministic hashes so identical code/options can be cached and referenced consistently.
- Compiler caching and disassembler caching should use distinct hashes because compiler flags and disassembler flags can vary independently.
- Async request handling should include a monotonic request identity or timestamp so stale responses cannot overwrite newer UI state.
- Overall request success should be green only when both compiler and disassembler attempts succeed.
- The response model should allow stage-level status so the UI can represent the edge case where compilation succeeds but disassembly fails.
- The UI should show two explicit stage icons for compiler and disassembler status once stage-level status exists.
- Stage status icons should be attached to their destination pane headers rather than only shown beside the global running indicator.
- Treat compiler/disassembler command execution as a security boundary before any hosted deployment.
- Avoid losing structured compiler phase data in the API.

## Roadmap

### Near Term

- Capture compile failures as first-class responses instead of losing useful output through command failure.
- Model command execution as attempts with explicit success and error channels.
- Break compiler phases out into collapsible UI sections backed by the existing `compiler.phases` response data.
- Research useful compiler and disassembler option checkboxes before adding option controls.

### Implementation Order

1. Implement the attempt/error response model first.
2. Surface attempt error channels through the API and UI.
3. Build collapsible compiler phase UI on top of the enriched response model.
4. Add async/status behavior after the request/attempt model is stable.

### Medium Term

- Introduce deterministic request hashing across source code plus selected compiler/disassembler options.
- Cache compiler results and disassembler results separately by deterministic hashes that include their respective flags.
- Use hashes as stable references for reruns, debugging, and possible future sharing.
- Add async submission with visible status lights for valid, invalid, running, and completed states.
- Add a running indicator outside the compiler and disassembler panes.
- Add debounce-on-edit auto-submission, with last-request-wins behavior for overlapping requests.
- Model stage-level success and failure so compiler-success/disassembler-failure can be represented without pretending the whole request succeeded.
- Show separate compiler and disassembler status icons for partial-success states.
- Place compiler and disassembler status icons in their respective pane headers.
- Keep the explicit `Run` action as a manual rerun control.
- Define repeated-request behavior so the UI renders only the newest request result even if older requests complete later.

### Long Term

- Support comparing two Scala snippets and their generated outputs side by side.
- Acknowledge that the first version of comparison can be approximated with two browser windows; built-in comparison should wait until the single-snippet generated-code journey is strong.

## Answered Questions

1. What is the primary user journey: learning Scala compiler phases, comparing generated bytecode, debugging compiler behavior, or building a shareable teaching/demo tool?

   Answer: the primary user journey is to see the generated code given some Scala code.

   Product synthesis: compiler phases and disassembly are not separate destinations; they are views into generated code. The product should optimize for moving from source snippet to generated output, then progressively add controls, navigation, caching, and comparison.

2. Which generated output should be the default first-class view: compiler phase trees, `javap` class summaries, `javap -c` bytecode, or another generated artifact?

   Answer: this is split by pane. Keep compiler phases on the left. Keep `javap` on the right, with optional `javap` checkboxes turned off by default.

   Product synthesis: the two-pane layout is part of the product model. The default should not force a single primary generated artifact; it should preserve the current left/right split while making each pane progressively richer.

3. Which compiler phase navigation model should come first: a vertical list of phase buttons, collapsible phase sections, tabs, or a search/filter over phases?

   Answer: the left pane should have collapsible headers for each phase.

   Product synthesis: collapsible phase sections preserve the full sequence of compiler output while letting users expand only the generated-code stages they care about. This is a better fit than tabs or a separate navigation list for the first structured UI pass.

4. Which compiler options should be exposed first as checkboxes?

   Answer: leave compiler flags as a research task. There are no known compiler checkboxes for now.

   Product synthesis: the compiler pane should prioritize phase structure and failure capture before exposing flags. Compiler flags can materially change output and cache keys, so the option set should be chosen deliberately.

5. Which `javap` or disassembler options should be exposed first as checkboxes?

   Answer: leave `javap` options as a research task for now.

   Product synthesis: the right pane should keep default `javap` behavior until the option set is researched. Any future disassembler options should remain off by default and should feed into the disassembler-specific cache hash.

6. What should happen in the UI when compilation fails?

   Answer: IO runs need to be enriched as attempts with a class that has an error channel. That error channel needs to be surfaced to the UI for rendering.

   Product synthesis: compile failure is still a meaningful generated-code attempt, not merely an HTTP failure. The compiler pane should render the failed compiler attempt, while the disassembler pane should make clear that disassembly was skipped when no class file was produced.

7. What validity states should the async status lights represent?

   Answer: status needs emoji-style indicators. Idle can show nothing for now, done can use a green checkbox, and invalid/error can use a red X. For now, all failure kinds can be red.

   Product synthesis: the UI needs both overall and stage-level status. The overall status should be green only when both compiler and disassembler succeed. If compilation succeeds but disassembly fails, the model should preserve that edge case, likely with separate stage icons or equivalent stage status.

8. For the compiler-success/disassembler-failure edge case, should the UI show two explicit stage icons, or one overall red icon with details in the panes?

   Answer: show two explicit stage icons.

   Product synthesis: separate compiler and disassembler icons make partial success visible without hiding useful compiler output. Overall success can still be derived from both stage statuses being successful.

9. Should the two stage icons be shown immediately near the global running indicator, in each pane header, or both?

   Answer: put the two stage icons in the pane headers.

   Product synthesis: compiler status belongs with the compiler pane, and disassembler status belongs with the `javap` pane. The separate running indicator can focus on request progress rather than stage result detail.

10. Should async submission happen only when the user presses `Run`, or eventually on edit/debounce?

   Answer: add debounce-on-edit auto-submission now; keep explicit `Run` as a manual rerun control.

   Product synthesis: autosubmit is part of the product now, and 300ms is a good default debounce. Restoring saved source should stay quiet until the user makes a new edit. In-flight work should be allowed to finish while stale results are ignored. The manual `Run` action remains useful for deliberate reruns and should not be removed.

11. Should the PRD prioritize implementation order as "attempt/error model first, then collapsible phase UI," or should the first visible win be the collapsible phase UI?

   Answer: prioritize the attempt/error model first, then collapsible phase UI.

   Product synthesis: the response model is the foundation for honest rendering of both success and failure. The collapsible phase UI should build on that contract instead of encoding assumptions from the current happy-path response.

12. What should the attempt model call its main fields?

   Answer: use `ProcessError(exitCode: Int, lines: List[String])` for the process error channel and model stage attempts as `Either[ProcessError, A]`.

   Product synthesis: avoid making `status` a second source of truth in the core model. Derive UI status from the `Either`, skipped stages, and running request state.

13. What client-side state should persist across page reloads in the first local-storage pass?

   Answer: persist the source textarea value in the first pass. When compiler and `javap` options are later implemented, persist those option selections too.

   Product synthesis: start with source-text persistence only. Do not persist transient run state or output panes. Treat option persistence as part of the option-feature work, and make those tasks depend on the local-storage foundation.

14. Should the first local-storage restore happen automatically on page load, or should the UI offer an explicit restore/reset choice when saved text exists?

   Answer: restore automatically on page load.

   Product synthesis: keep the first persistence flow simple and predictable. If users later need reset behavior, add a separate clear action rather than complicating the initial restore experience.

15. Should local storage support only one current working snippet, or should the product eventually support multiple saved drafts in the browser?

   Answer: support only one current working snippet for now.

   Product synthesis: multiple drafts are out of scope for the first persistence feature. Treat browser persistence as a single working-snippet convenience, not a draft-management system.

16. Should source persistence update on every edit, on explicit save, or only after a successful compile?

   Answer: save only after a successful compile.

   Product synthesis: local storage should represent the most recently successful working snippet rather than every transient edit. Failed compile attempts should leave the last successful saved snippet untouched.

17. When compiler or `javap` options are eventually added, should those option selections persist immediately when changed, or only after a successful compile?

   Answer: persist option selections immediately when changed.

   Product synthesis: option selections behave like sticky UI preferences, not saved source content. They should restore independently of compile success so menu state stays predictable across reloads.

## Open Questions

No open product questions currently block the next implementation tasks. The current task graph is specific enough to continue with `next-task`.

## Interview Log

### 2026-05-02

- Request: continually interview the user about project goals, maintain this PRD, answer discoverable questions through code exploration, and include a recommended answer for each question.
- Repository-derived facts were added from README, build configuration, web routes, core compiler/disassembler wrappers, response models, JavaScript, and CSS.
- User clarified that the primary journey is seeing generated code from Scala code.
- User added long-term goals: compiler/disassembler option checkboxes, compile failure capture, deterministic request hashing and caching, phase UI elements, and eventual comparison of two code snippets.
- User clarified that output defaults are pane-specific: compiler phases on the left, `javap` on the right, with right-pane optional checkboxes turned off by default.
- User clarified that compiler and disassembler cache hashes should be distinct because flags differ between those stages.
- User clarified that the left pane should use collapsible headers for each compiler phase.
- User clarified that compiler flags should remain a research task with no known checkboxes for now.
- User clarified that `javap` options should also remain a research task for now.
- User clarified that IO runs should become attempt values with explicit error channels surfaced to the UI.
- User added long-term goals for async submission with status lights and repeated-request behavior where the newest request wins in the UI.
- User added a goal for a separate running indicator outside the two output panes, because those panes may still contain existing results while a new request runs.
- User clarified async status icons: idle can be blank, done can be green checkbox, invalid/error can be red X, and all failures can be red for now.
- User raised the compiler-success/disassembler-failure edge case; the model should support stage-level status, with overall green only if both stages succeed.
- User clarified that compiler-success/disassembler-failure should be represented with two explicit stage icons.
- User clarified that the two stage icons should be shown in their respective pane headers.
- User clarified that async submission should remain tied to the explicit `Run` action for now.
- User clarified that implementation should prioritize the attempt/error model first, then collapsible phase UI.
- User questioned whether `status` differs meaningfully from `error`; the working model is now to keep error as `code` plus `lines` and derive UI status where possible.
- User clarified the concrete attempt shape: `ProcessError(exitCode: Int, lines: List[String])` and `Either[ProcessError, A]`.

### 2026-05-11

- Interview loop resumed after the task wording update from cookies to local storage.
- Current unresolved near-term product question is the scope of client-side persistence in the first local-storage pass.
- User decided that the first persistence pass should restore only the source textarea.
- User decided that future compiler and `javap` option tasks should depend on local-storage support and persist their selections once implemented.
- User decided that saved source should restore automatically on page load.
- User decided that browser persistence should cover only one working snippet, not multiple saved drafts.
- User decided that source persistence should update only after a successful compile.
- User decided that compiler and `javap` menu changes should save to local storage immediately.
