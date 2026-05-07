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

- [ ] Class-file discovery searches recursively under the compile output directory
- [ ] Nested package class files are included in disassembly input
- [ ] No-class behavior remains unchanged when no class files exist anywhere in the tree

## api-attempt-schema

- [x] `/compile` returns explicit attempt state for compiler and disassembler stages
- [x] Response schema is stable and machine-readable for both success and failure paths

## compile-failure-ui

- [x] Left pane shows compiler failure details from API data, including exit code and lines
- [x] Compile failure state renders without breaking right-pane behavior

## skipped-disassembly-ui

- [ ] Right pane has a distinct skipped state
- [ ] Skip reason is shown when class generation did not occur

## collapsible-phases

- [ ] Compiler phases render as collapsible sections keyed by phase hint
- [ ] Expand/collapse state is deterministic on rerender

## pane-stage-icons

- [ ] Pane headers show stage icons derived from attempt state (success/failure/skipped/running)
- [ ] Icons update correctly across repeated runs

## running-indicator

- [ ] A separate running indicator appears outside both panes during active requests
- [ ] Indicator clears on terminal response paths (success and failure)

## last-request-wins

- [ ] Stale responses are ignored when a newer request is in flight
- [ ] Behavior is verified with an intentional out-of-order response scenario

## explicit-run-async

- [ ] Async execution is triggered only by explicit Run submission

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

## compare-snippets

- [ ] UI supports two snippet inputs with isolated compiler/disassembler outputs per snippet
- [ ] Comparison view enables practical side-by-side inspection without cross-contamination
