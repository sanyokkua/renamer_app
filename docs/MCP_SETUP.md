# MCP (Model Context Protocol) Server

This project uses the **py-search-helper** MCP server to provide web search and content extraction capabilities to Claude Code.

## What MCP Provides

The py-search-helper MCP enables Claude to:

- Search the web for API documentation and library references
- Extract content from documentation pages
- Find up-to-date information beyond training data
- Look up best practices and design patterns

## Available Tools

### get_engines()

List available search engines.

**Returns:** Available search engines (DuckDuckGo, Wikipedia, PySide6 docs)

### search_web(engine, query, max_results)

Search using a specific engine.

**Parameters:**

- `engine` - Engine ID: "ddgs" (DuckDuckGo), "wikipedia", "pyside"
- `query` - Search query string
- `max_results` - Maximum results (1-30, default: 10)

**Returns:** Markdown-formatted search results with titles, URLs, and descriptions

### search_web_ddg(query, max_results)

Convenience method for DuckDuckGo search.

**Parameters:**

- `query` - Search query string
- `max_results` - Maximum results (1-30, default: 10)

### open_page(url, max_chars)

Extract content from a URL as Markdown.

**Parameters:**

- `url` - URL to extract content from
- `max_chars` - Maximum characters to return (default: 500)

**Returns:** Page content converted to Markdown

## When to Use

**Use MCP for:**

- API documentation lookup (Apache Tika, Metadata Extractor, JavaFX, Guice, Mockito, JUnit)
- Java best practices and design patterns
- Library examples and tutorials
- Information beyond Claude's training data (newer features, versions)
- Technical concepts from Wikipedia

**Don't use MCP for:**

- Information already in CLAUDE.md or project documentation
- Basic Java/programming concepts
- Project-specific code questions
- Information in recent conversation context

## Configuration

MCP is configured in `.mcp.json`:

```json
{
  "mcpServers": {
    "py-search-helper": {
      "command": "uv",
      "args": ["run", "py-search-helper-mcp"],
      "description": "Web search and content extraction"
    }
  }
}
```

## Enabling MCP

When starting Claude Code, approve the MCP server when prompted.

Or enable in `.claude/settings.local.json`:

```json
{
  "enableAllProjectMcpServers": true
}
```

## Verification

Check that `uv` is installed and MCP server runs:

```bash
which uv
uv run py-search-helper-mcp --help
```
