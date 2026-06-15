# Rendering Backends — Selection, Precedence & Transparency

The skill can render through three backends. They produce the same output; they
differ in **install cost** and, crucially, **where your source goes**.

| Backend | Endpoint / command | Install | Source leaves your machine? |
|---|---|---|---|
| **Public Kroki** (default) | `https://kroki.io/plantuml/{png,svg}` | just `curl` | **Yes** — POSTed to a third-party service |
| **Local Kroki** (Docker) | `http://localhost:8000/plantuml/{png,svg}` | `docker run -d -p 8000:8000 yuzutech/kroki` | No |
| **Local `plantuml.jar`** | `java -jar plantuml.jar diagram.puml` | Java + Graphviz + jar | No |

## Selection / precedence

Decide the backend *before* Step 4:

1. **Honor an explicit choice** — if the user named a backend, or `KROKI_URL` is
   set in the environment, use it. (Swap `https://kroki.io` → that base URL in
   the Step 4 commands.)
2. **Sensitive content** — internal systems, secrets, proprietary names, unreleased
   architecture → prefer a **local** backend. If none is available, say so and ask
   before sending the source to public Kroki.
3. **Otherwise** → default to public Kroki (zero-install).

Use one base URL throughout a session; don't mix.

## No silent downgrade

This is the rule that makes the choice trustworthy:

- **Don't quietly fall back.** If the chosen backend is unavailable (local Kroki
  not running, no `java`/jar), do *not* silently switch to another. Tell the user
  which backend failed and what the alternatives are, then proceed only with a
  sensible default or their go-ahead.
- **Report the path in Step 8.** State which backend rendered and whether the
  source left the machine — e.g. *"rendered via public Kroki (source uploaded to
  kroki.io)"* vs *"rendered via local Kroki (stayed on your machine)."* The user
  should never have to guess whether their diagram crossed the network.
