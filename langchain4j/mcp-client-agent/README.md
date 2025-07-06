## MCP Client Agent
> Langgraph4j and MCP integration using [langchain4j](https://docs.langchain4j.dev) and [mcp/postgres](https://hub.docker.com/mcp/server/postgres/overview)



### Getting Started

**Start Postgres docker image and load the Database** (see `init_schema.sql`)
```
docker compose -f src/main/docker/docker-compose.yml up
```

**Run application** (see `MCPClientAgent.java` )

```
mvn  package exec:java
```


## References

* [Find awesome MCP ](https://mcp.so)
* [MCP Hub](https://hub.docker.com/mcp)