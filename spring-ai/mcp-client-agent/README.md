## MCP Client Agent
> Langgraph4j and MCP integration using [Spring AI](https://spring.io/projects/spring-ai) and [mcp/postgres](https://hub.docker.com/mcp/server/postgres/overview)


### Getting Started

**Start Postgres docker image and load the Database** (see `init_schema.sql`)
```
docker compose -f src/main/docker/docker-compose.yml up
```

**Run application** 

by default it use `ollama:llama3.1`

**Console**
```
mvn package spring-boot:run@console
```

**Langgraph4j Studio**
```
mvn package spring-boot:run@studio
```

for `openai` 

**Console**
```
mvn package spring-boot:run@console -Popenai
```

**Langgraph4j Studio**
```
mvn package spring-boot:run@studio -Popenai
```

## References

* [Find awesome MCP ](https://mcp.so)
* [MCP Hub](https://hub.docker.com/mcp)