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

- [ ] API response replaces prior raw stage `lines` payloads with grouped-line schema fields for compiler and disassembly stages
- [ ] Compiler and disassembly grouped schemas use separate role enums (compiler: `plain|def|val|var`; decompiler: `plain|field|method`)
- [ ] Group model is line-list compatible: each group contains ordered lines, and stage output remains reconstructible without loss

## compiler-group-parser-server

- [ ] Server groups compiler output into contiguous same-role runs using compiler-role enum
- [ ] Role detection covers declaration/signature grouping for `def`, `val`, and `var`; unmatched and empty lines are preserved as `plain`
- [ ] Output ordering matches exact compiler emission order with no cross-document regrouping

## disassembly-group-parser-server

- [ ] Server groups per-class disassembly output into contiguous same-role runs using decompiler-role enum
- [ ] Role detection covers `field` and `method`; unmatched and empty lines are preserved as `plain`
- [ ] Init-specific highlighting for `<init>` / `<clinit>` is not added in v1 without explicit user confirmation

## ui-structured-group-rendering

- [ ] UI renders compiler and disassembly panes from structured grouped-line fields only (no client-side parsing of raw output text)
- [ ] Compiler pane keeps existing collapsible-phase behavior while rendering role groups in source order
- [ ] CSS-only role palettes are applied with top-down round-robin assignment per render
- [ ] Fallback `plain` groups render with no highlight and preserve original line text/spacing

## member-highlighting-tests-docs

- [ ] Tests cover server grouped-schema serialization for both stage hierarchies and role enums
- [ ] Tests verify fallback behavior for unmatched and empty lines, including preserved ordering and contiguous grouping
- [ ] Asset/UI tests verify rendering consumes structured groups without client-side text parsing
- [ ] Documentation reflects server-side parsing, direct schema replacement, and declaration/signature-only highlighting scope

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

## compiler-options

- [ ] UI exposes researched `scalac` options and sends selections through request/hash
- [ ] Backend honors selected `scalac` options during execution
- [ ] Selected `scalac` options persist immediately on menu change via local storage and restore on page load

## javap-options

- [ ] UI exposes researched `javap` options and sends selections through request/hash
- [ ] Backend honors selected `javap` options during execution
- [ ] Selected `javap` options persist immediately on menu change via local storage and restore on page load

## javap-line-numbers

- [ ] `javap` execution includes line number table output for compiled classes
- [ ] Backend parses line number entries into structured class/method mappings
- [ ] API response exposes parsed line mappings without breaking existing disassembly output

## compare-snippets

- [ ] UI supports two snippet inputs with isolated compiler/disassembler outputs per snippet
- [ ] Comparison view enables practical side-by-side inspection without cross-contamination
