# libra4s

Explore generated JVM output from Scala code in your browser

`libra4s` is a small local web app for understanding what Scala code becomes after compilation.
Paste Scala source, run it, then inspect:

- compiler phase output (`scalac -Vprint:all`)
- disassembly output (`javap -c`)

## Features

- Browser-based Scala snippet runner with explicit `Run` execution
- Compiler phase output from `scalac -Vprint:all`, grouped for easier inspection
- `javap -c` disassembly output in a dedicated side-by-side pane

## Prerequisites

- Java (JDK 21 is used in this project)
- `sbt`
- `scalac` available on your `PATH`
- `javap` available on your `PATH`

## Run locally

```bash
sbt web/run
```

The service starts on [http://localhost:8080](http://localhost:8080)

## How to use

1. Open `http://localhost:8080`
2. Paste a small Scala snippet into the source box
3. Click `Run`
4. Read results in two panes:
   - left: compiler phases
   - right: `javap` disassembly

## Colophon

This project was named after the `Libra` spell in Final Fantasy, which reveals data about enemies.

This project was agentically engineered with GitHub Copilot in IntelliJ and ChatGPT Codex.
