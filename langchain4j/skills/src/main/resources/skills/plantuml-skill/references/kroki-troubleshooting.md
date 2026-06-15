# Kroki Troubleshooting & Safe Subset

Deep reference for the Step 5 self-check loop. Read this when a render returns
`400` (or a broken file) and fixing the single flagged line did not resolve it.

Kroki renders PlantUML server-side with a recent PlantUML + Graphviz, so you do
not need anything installed locally beyond `curl`. Two hard limits to remember:

- **No network fetches.** Kroki will not resolve a remote `!includeurl https://…`.
  Use the bundled `!include <C4/…>` standard-library form instead.
- **The output file is the error channel.** On a syntax error Kroki returns
  HTTP `400` and writes the reason into the file you asked it to `-o`. Always
  `cat` that file — it names the offending line.

## What renders reliably on Kroki (safe subset)

These diagram types and features are dependable through the public Kroki API:

- Sequence — including `alt` / `opt` / `loop` / `par` fragments and `activate` / `deactivate`
- Component / package, with modest `skinparam` styling
- Class, object, deployment, use case
- Activity (the modern `start` … `stop` syntax)
- State machines
- ER / entity
- C4 via the bundled `!include <C4/C4_Context|C4_Container|C4_Component>`
- Mind map, Gantt

Treat exotic shapes, heavy `skinparam` blocks, embedded sprites/icons, and
custom fonts as **optional** — they are the first things to fail and the first
things to drop when degrading (below).

## Failure-degradation ladder

If re-rendering after a targeted line fix still returns `400` (or the layout is
unusable), simplify in this order, re-running Step 4 after each step. Stop as
soon as it renders:

1. **Remove exotic shapes** — fall back to `rectangle` / `component` / `node`.
2. **Strip styling** — delete `skinparam` blocks and `!theme`; render plain first.
3. **Remove notes** — `note left/right/over` lines are a common parse trap.
4. **Simplify labels** — drop special characters; wrap any remaining label in `"…"`.
5. **Reduce edges** — cut redundant relationships; a smaller graph parses and lays out more reliably.
6. **Switch engine** — if a feature simply isn't supported, render the core idea
   in a simpler diagram type rather than forcing the current one.

Re-add the dropped styling only once the structure renders cleanly.

## Common syntax errors by diagram type

### General (any diagram)
- Missing `@startuml` / `@enduml` (or `@startmindmap` / `@startgantt`) wrapper.
- Smart quotes / non-breaking spaces / tabs pasted from a doc or chat — retype the line.
- Unbalanced `{ }` in `package` / `rectangle` / class bodies.

### Sequence
- `alt` / `opt` / `loop` / `par` / `group` each need a matching `end`.
- Reversed arrow: `B <- A` sends *to* B's left; use `A -> B` for left-to-right intent.
- Participants render in first-mention order — declare `participant` at the top to fix left-to-right order.

### Class
- Relationship glyphs: `<|--` inheritance, `*--` composition, `o--` aggregation, `..|>` realization. Mixing the dashes/heads silently changes meaning.
- Multiplicities must be quoted: `User "1" --> "*" Order`.

### Activity
- Do not mix the legacy `(*)` syntax with the modern `start` / `:action;` / `stop` syntax in one diagram.
- Every `if (…) then` needs `endif`; every `repeat` needs `repeat while`.

### State
- Use `[*]` for the initial/final pseudo-state; `[ ]` or `()` will fail.
- Composite states need `state Name { … }` braces, not indentation.

### ER / entity
- Crow's-foot cardinality goes between the entities: `user ||--o{ order`.
- Attribute block separator is `--` on its own line inside the `entity { }` body.

### Component
- Overlap/crowding is a layout symptom, not an error — add `together { }` or a direction hint (`left to right direction`) before assuming the source is wrong.

### C4
- `400` with "cannot include" → you used a remote `!includeurl`; switch to the bundled `!include <C4/C4_Context>`.
- Macro arity matters: `System(alias, "Label", "Description")` — a missing argument throws.
