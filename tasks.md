## prd-draft

- [x] A product requirements document exists and defines project direction
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

- [x] Editing the source triggers a compile request after a 2000ms debounce
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

- [x] UI renders compiler and disassembly panes from structured grouped-line fields only, with no client-side role parsing
- [x] Compiler pane keeps collapsible-phase behavior while rendering each server-provided group in source order
- [x] CSS maps semantic roles to green, red, yellow, or blue and consecutive same-role groups alternate full and faded variants
- [x] Fallback `plain` groups render with no background or colored edge and preserve original line text and spacing

## member-highlighting-server-tests

- [x] Server tests cover grouped-schema serialization for both stage hierarchies and role enums
- [x] Dedicated compiler and javap parser specs verify fallback behavior, exact ordering, and indentation-owned blocks
- [x] Saved `case class Dog(n: String)` compiler and javap fixtures make expected grouping reviewable without running the web server

## member-highlighting-ui-tests

- [x] Asset tests verify rendering consumes structured groups without client-side text parsing
- [x] Tests cover semantic role classes, consecutive full/faded variants, and transparent `plain` groups
- [x] UI asset tests remain separate from server parser expectations

## member-highlighting-docs

- [x] `PRD.html` documents server-side parsing and direct grouped-schema rendering
- [x] `PRD.html` documents top-level declaration/member grouping and indentation-owned bodies
- [x] `PRD.html` documents semantic colors, alternating variants, and no-highlight `plain` fallback behavior

## hash-input-model

- [ ] Compiler hash input model includes source text and compiler-relevant option fields
- [ ] Disassembler hash input model is separate and includes source identity, compiler option state, and javap option state
- [ ] Hash input models make the current no-option/default-option state explicit without depending on future UI controls

## hash-cacheable-instances

- [ ] Compiler and disassembler hash input models each have a `Cacheable` instance using the existing hash utilities
- [ ] Canonical strings include a schema/version prefix so future hash changes can be made deliberately
- [ ] Tests verify identical inputs produce identical slugs and relevant source or option changes produce different slugs

## keyed-cache-storage

- [ ] `JsonFileCache` can read a decoded value by a distinct typed key and write a value under that key
- [ ] Cache filenames derive only from the key's `Cacheable` slug, not from serialized result data
- [ ] Focused tests cover hit, miss, overwrite, decode failure, and distinct keys without stage integration

## compiler-cache-payload

- [ ] Compiler cache payload represents compiler success or process failure without depending on temporary paths
- [ ] Successful payloads preserve compiler phase data and every generated class artifact with a safe relative identity and byte content
- [ ] Payload codecs round-trip representative success, no-class, and failure cases
- [ ] Restoring artifacts cannot write outside the allocated compilation directory

## hash-request-threading

- [ ] Compile route builds compiler and disassembler hash input values from the current request and selected/default options
- [ ] Disassembler identity is derived from source identity plus compiler and javap option state, without hashing class-file bytes
- [ ] Hash input construction is covered without changing compile/disassemble execution behavior
- [ ] No cache reads or writes are introduced in this task

## compiler-cache

- [ ] Compiler cache hit restores the compiler attempt and any class artifacts without invoking `scalac`
- [ ] Compiler cache miss invokes `scalac` once and stores the resulting success or process failure payload
- [ ] Compiler-relevant source or option changes miss the cache
- [ ] Route or service tests cover hit, miss, cached failure, and restored artifacts used by the downstream stage

## disassembler-cache

- [ ] Disassembler cache hit returns the stored javap attempt without invoking `javap`
- [ ] Disassembler cache miss uses current or restored compiler artifacts, invokes `javap`, and stores success or process failure
- [ ] Source identity, compiler option state, or javap option changes miss the cache
- [ ] Tests cover hit, miss, partial failure, and deterministic reuse without hashing class-file bytes

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

## compare-snippets-ui-layout

- [ ] Comparison mode exposes two source inputs while the default mode remains the existing single-snippet workflow
- [ ] Each snippet owns separate browser request identity, running state, and stage icons
- [ ] Layout remains usable for long compiler and disassembler output at supported viewport widths

## compare-snippets-ui

- [ ] Each snippet result renders into its own compiler and disassembler panes
- [ ] Existing collapsible phase, semantic grouping, and stage icon behavior applies independently to each snippet
- [ ] A stale response from either snippet cannot overwrite the other snippet or a newer result

## compare-snippets-tests-docs

- [ ] Tests cover side-by-side comparison behavior across successful and failed snippet attempts
- [ ] Tests cover independent request races, stage icons, collapsible phases, and semantic groups
- [ ] Documentation explains comparison scope and preserves the single-snippet default
