## prd-draft

- [x] `PRD.md` exists and defines project direction
- [x] Initial task DAG is established in `tasks.csv` and detailed in `tasks.md`

## process-error

- [x] Process error channel includes exit code and output lines
- [x] Error payload is used by runner and downstream APIs

## process-runner-attempt

- [x] Process runner returns `Either` attempt instead of throwing on non-zero exits
- [x] Success and failure outputs are preserved in structured form

## compiler-attempt

- [x] Compiler APIs return structured success or process error attempts
- [x] Compiler phase parsing remains available on success

## disassembler-attempt

- [x] Disassembler API returns structured success or process error attempts
- [x] Disassembler failures preserve machine-readable error data

## recursive-class-search

- [x] Class-file discovery searches recursively under the compile output directory
- [x] Nested package class files are included in disassembly input
- [x] No-class behavior remains unchanged when no class files exist anywhere in the tree

## api-attempt-schema

- [x] `/compile` returns explicit attempt state for compiler and disassembler stages
- [x] Response schema is stable and machine-readable for both success and failure paths

## compile-failure-ui

- [x] Left pane shows compiler failure details from API data, including exit code and lines
- [x] Compile failure state renders without breaking right-pane behavior


## collapsible-phases

- [x] Compiler phases render as collapsible sections keyed by phase hint
- [x] Expand/collapse state is deterministic on rerender

## pane-stage-icons

- [x] Pane headers show stage icons derived from attempt state (success/failure/skipped/running)
- [x] Icons update correctly across repeated runs

## running-indicator

- [x] While a run is in flight, show hourglass icon and status text immediately left of the `Run` button
- [x] Reuse the same hourglass visual already used for running stage state
- [x] Hide or clear the indicator on every terminal path (success, compile failure, disassembly failure, malformed request)

## last-request-wins

- [x] Stale responses are ignored when a newer request is in flight
- [x] Behavior is verified with an intentional out-of-order response scenario

## explicit-run-async

- [x] Async execution is triggered only by explicit Run submission

## persist-source-local-storage

- [x] Source textarea value is written to local storage only after a successful compile
- [x] Saved local storage value is restored automatically into the source textarea on page load
- [x] Failed compile attempts do not overwrite the last successfully saved local storage value
- [x] Empty or missing local storage value does not overwrite the placeholder or default UI state

## debounce-autosubmit

- [x] Editing the source triggers a compile request after a 300ms debounce
- [x] Restoring saved source on page load does not trigger autosubmit until the user makes a new edit
- [x] A manual `Run` action still submits immediately for reruns
- [x] When a newer auto-submit starts, stale in-flight responses do not overwrite the latest result

## member-group-schema-server

- [x] API response replaces prior raw stage `lines` payloads with grouped-line schema fields for compiler and disassembly stages
- [x] Compiler and disassembly grouped schemas use separate role enums (compiler: `plain|def|val|var`; decompiler: `plain|field|method`)
- [x] Group model is line-list compatible: each group contains ordered lines, and stage output remains reconstructible without loss

## compiler-group-parser-server

- [x] Server groups each compiler declaration into a separate block using compiler-role enum
- [x] Role detection covers top-level `def`, `val`, and `var`; each declaration consumes more-indented body lines without parsing nested declarations
- [x] Output ordering matches exact compiler emission order with no cross-document regrouping

## disassembly-group-parser-server

- [x] Server groups each per-class disassembly member into a separate block using decompiler-role enum
- [x] Role detection covers `field` and `method`; each member consumes its more-indented bytecode body while unmatched lines remain `plain`
- [x] Init-specific highlighting for `<init>` / `<clinit>` is not added in v1 without explicit user confirmation

## ui-structured-group-rendering

- [ ] UI renders compiler and disassembly panes from structured grouped-line fields only (no client-side parsing of raw output text)
- [ ] Compiler pane keeps existing collapsible-phase behavior while rendering role groups in source order
- [ ] CSS-only role palettes are applied with top-down round-robin assignment per render
- [ ] Fallback `plain` groups render with no highlight and preserve original line text/spacing

## member-highlighting-server-tests

- [ ] Server tests cover grouped-schema serialization for both stage hierarchies and role enums
- [ ] Tests verify fallback behavior for unmatched and empty lines
- [ ] Tests verify preserved ordering and contiguous grouping without exercising UI rendering

## member-highlighting-ui-tests

- [ ] Asset or UI tests verify rendering consumes structured groups without client-side text parsing
- [ ] Tests cover fallback `plain` group rendering and role class assignment
- [ ] Tests do not modify server parser expectations

## member-highlighting-docs

- [ ] Documentation reflects server-side parsing and direct schema replacement
- [ ] Documentation states declaration/signature-only highlighting scope
- [ ] Documentation describes fallback no-highlight line behavior

## hash-input-model

- [ ] Compiler hash input model includes source text and compiler-relevant option fields
- [ ] Disassembler hash input model is separate from compiler hash input and includes disassembler-relevant option fields
- [ ] Hash input models make the current no-option/default-option state explicit without depending on future UI controls

## hash-cacheable-instances

- [ ] Compiler and disassembler hash input models each have a `Cacheable` instance using the existing hash utilities
- [ ] Canonical strings include a schema/version prefix so future hash changes can be made deliberately
- [ ] Tests verify identical inputs produce identical slugs and source or option changes produce different slugs

## hash-request-threading

- [ ] Compile route builds compiler and disassembler hash input values from the current request and selected/default options
- [ ] Hash input construction is covered without changing compile/disassemble execution behavior
- [ ] No cache reads or writes are introduced in this task

## compiler-cache

- [ ] Compiler stage uses hash-based cache with correct hit/miss behavior
- [ ] Cache invalidates when compiler-relevant inputs change

## disassembler-cache

- [ ] Disassembler stage uses hash-based cache with correct hit/miss behavior
- [ ] Cache invalidates when disassembler-relevant inputs change

## option-research

- [ ] Document selected `scalac` and `javap` options with rationale for this workflow
- [ ] Document rejected options only when rejection affects later UX choices

## compiler-options-model

- [ ] Compiler option selection model represents researched options and defaults
- [ ] Compiler option defaults preserve current `scalac` behavior when no option is selected
- [ ] Compiler option selections are included in compiler hash input construction

## compiler-options-api

- [ ] Compile request contract accepts compiler option selections
- [ ] Missing compiler option fields decode to the model defaults
- [ ] API contract tests cover selected and default compiler option payloads

## compiler-options-execution

- [ ] Backend passes selected compiler options into `scalac` command construction
- [ ] Existing required compiler flags remain present unless explicitly superseded by researched options
- [ ] Focused core or route test proves selected options reach the compiler command

## compiler-options-storage

- [ ] Compiler option selections persist immediately on menu change via local storage
- [ ] Saved compiler option selections restore on page load
- [ ] Storage behavior is verified without requiring backend execution changes

## compiler-options-ui

- [ ] UI exposes researched compiler option controls
- [ ] UI submits selected compiler option values through the compile request
- [ ] UI keeps current default compiler behavior when no options are selected

## compiler-options-tests-docs

- [ ] Tests cover compiler option request decoding execution storage and UI behavior at focused surfaces
- [ ] README documents available compiler options and default behavior
- [ ] Documentation reflects immediate option persistence via local storage

## javap-options-model

- [ ] Javap option selection model represents researched options and defaults
- [ ] Javap option defaults preserve current right-pane behavior when no option is selected
- [ ] Javap option selections are included in disassembler hash input construction

## javap-options-api

- [ ] Compile request contract accepts javap option selections
- [ ] Missing javap option fields decode to the model defaults
- [ ] API contract tests cover selected and default javap option payloads

## javap-options-execution

- [ ] Backend passes selected javap options into disassembler command construction
- [ ] Existing required javap flags remain present unless explicitly superseded by researched options
- [ ] Focused core or route test proves selected options reach the javap command

## javap-options-storage

- [ ] Javap option selections persist immediately on menu change via local storage
- [ ] Saved javap option selections restore on page load
- [ ] Storage behavior is verified without requiring backend execution changes

## javap-options-ui

- [ ] UI exposes researched javap option controls
- [ ] UI submits selected javap option values through the compile request
- [ ] UI keeps current default right-pane behavior when no options are selected

## javap-options-tests-docs

- [ ] Tests cover javap option request decoding execution storage and UI behavior at focused surfaces
- [ ] README documents available javap options and default behavior
- [ ] Documentation reflects immediate option persistence via local storage

## javap-line-numbers

- [ ] `javap` execution includes line number table output for compiled classes
- [ ] Backend parses line number entries into structured class/method mappings
- [ ] API response exposes parsed line mappings without breaking existing disassembly output

## compare-snippets-model

- [ ] Two-snippet request/response model represents isolated left and right snippet inputs
- [ ] Model preserves separate compiler and disassembler stage attempts per snippet
- [ ] Defaults preserve the existing single-snippet flow until comparison UI is used

## compare-snippets-execution

- [ ] Backend runs compile and disassemble flow independently for each compared snippet
- [ ] Failures in one snippet do not overwrite or hide the other snippet result
- [ ] Focused test proves outputs remain associated with the correct snippet

## compare-snippets-ui

- [ ] UI supports two snippet inputs with isolated compiler/disassembler outputs per snippet
- [ ] Existing collapsible phase and stage icon behavior applies independently to each snippet
- [ ] Comparison view enables practical side-by-side inspection without cross-contamination

## compare-snippets-tests-docs

- [ ] Tests cover side-by-side comparison behavior across successful and failed snippet attempts
- [ ] Documentation explains comparison scope and the two-browser-window fallback no longer being required
