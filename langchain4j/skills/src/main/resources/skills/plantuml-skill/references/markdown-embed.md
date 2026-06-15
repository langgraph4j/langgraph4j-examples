# Rendering PlantUML Embedded in Markdown (Extract → Render → Rewrite)

Use when a Markdown file already contains PlantUML and the user wants images
instead of raw source — "render the diagrams in this README", or "prepare this
doc for Confluence / Notion" (those platforms don't render fenced PlantUML).

## What to find

1. Fenced blocks: ` ```plantuml ` or ` ```puml ` … ` ``` `
2. Linked sources: `![alt](path/to/diagram.puml)`

## Steps (per diagram)

1. **Extract** the source — the fenced body, or read the linked `.puml`.
2. **Name it stably** — slug from the nearest heading or the alt text, e.g.
   `images/<doc>-<slug>.png`. Stable names keep re-runs idempotent (no duplicate
   files piling up each time the doc is reprocessed).
3. **Render + validate** via Workflow Step 4–5, writing into the images dir.
4. **Rewrite** the Markdown:
   - fenced block → replace with `![alt](images/<doc>-<slug>.png)`
   - linked `.puml` → repoint the link to the rendered image
5. **Keep the source.** Either leave the original `.puml` on disk, or tuck the
   source under a collapsed block so the diagram stays editable:

   ````markdown
   ![Login flow](images/readme-login-flow.png)

   <details><summary>PlantUML source</summary>

   ```plantuml
   ...original source...
   ```

   </details>
   ````

## Notes

- **SVG vs PNG** — SVG keeps text crisp and is smaller for line diagrams; PNG
  is the safest for pasting into Confluence / Notion. Pick per target.
- **Report, don't silently skip** — process every match and report a count
  ("rendered 4 of 4 diagrams"); surface any that failed Step 5 validation
  instead of dropping them.
