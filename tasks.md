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

- [ ] Stale responses are ignored when a newer request is in flight
- [ ] Behavior is verified with an intentional out-of-order response scenario

## explicit-run-async

- [ ] Async execution is triggered only by explicit Run submission

## persist-source-cookie

- [ ] Source textarea value is written to a cookie when the user edits content
- [ ] Saved cookie value is restored into the source textarea on page load
- [ ] Empty or missing cookie does not overwrite the placeholder or default UI state

## hash-model

- [ ] Hash input is canonical and deterministic for equivalent source/options
- [ ] Hash includes all fields that affect compiler/disassembler outputs

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

## javap-options

- [ ] UI exposes researched `javap` options and sends selections through request/hash
- [ ] Backend honors selected `javap` options during execution

## javap-line-numbers

- [ ] `javap` execution includes line number table output for compiled classes
- [ ] Backend parses line number entries into structured class/method mappings
- [ ] API response exposes parsed line mappings without breaking existing disassembly output

## compare-snippets

- [ ] UI supports two snippet inputs with isolated compiler/disassembler outputs per snippet
- [ ] Comparison view enables practical side-by-side inspection without cross-contamination
