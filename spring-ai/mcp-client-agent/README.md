## MCP Client Agent
> Langgraph4j and MCP integration using Spring AI

### Getting Started

**Start Postgres docker image and load the Database** (see `init_schema.sql`)
```
docker compose -f src/main/docker/docker-compose.yml up
```

**Run application** (see `MCPClientAgentApplication.java` )

```
mvn package spring-boot:run
```
