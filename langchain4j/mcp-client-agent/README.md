## MCP Client Agent
> Langgraph4j and MCP integration using langchain4j

### Getting Started

**Start Postgres docker image and load the Database** (see `init_schema.sql`)
```
docker compose -f src/main/docker/docker-compose.yml up
```

**Run application** (see `MCPClientAgent.java` )

```
mvn  package exec:java
```
