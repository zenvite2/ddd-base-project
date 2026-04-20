# GitNexus Research Report

**Date:** 2026-03-30
**Project:** GitNexus Research
**Author:** Researcher

---

## 1. What is GitNexus?

**GitNexus** is a client-side knowledge graph creator that indexes codebases and creates a structured representation of code relationships. It's designed specifically to provide AI agents (Cursor, Claude Code, Codex, Windsurf) with comprehensive architectural awareness and codebase understanding.

### Core Problem Solved
Traditional AI coding assistants can edit code but lack deep understanding of:
- Architectural dependencies
- Call chains and execution flows
- Relational context between modules
- Impact of changes across the codebase

GitNexus bridges this gap by precomputing structural analysis at index time.

---

## 2. Key Features

| Feature | Description |
|---------|-------------|
| **Code Parsing** | Tree-sitter based extraction (TypeScript, JavaScript, Python, Java, etc.) |
| **Dependency Resolution** | Resolves imports, function calls, type relationships |
| **Functional Communities** | Groups related symbols via clustering |
| **Execution Flow Tracing** | Traces call chains from entry points |
| **MCP Integration** | 7 MCP tools (impact analysis, context exploration, change detection, multi-file refactoring) |
| **Dual Modes** | CLI + MCP for editors, or web UI for quick analysis |
| **Auto-Generation** | Creates repository wikis and agent skills |

### Two Usage Approaches
1. **CLI + MCP**: Local indexing with integration into editors (recommended for production)
2. **Web UI**: Browser-based explorer for quick analysis (no installation needed)

---

## 3. Installation & Setup

### Quick Start
```bash
npx gitnexus analyze
```
Single command that:
- Indexes repository
- Auto-configures MCP integration
- Generates `AGENTS.md` and `CLAUDE.md` context files

### Editor Configuration

**One-time setup:**
```bash
npx gitnexus setup
```
Auto-detects and configures for:
- Claude Code
- Cursor
- Codex
- OpenCode
- Windsurf

**Manual Configuration (Cursor example):**
```json
{
  "mcpServers": {
    "gitnexus": {
      "command": "npx",
      "args": ["-y", "gitnexus@latest", "mcp"]
    }
  }
}
```

### Core Commands
```bash
gitnexus analyze [path]           # Index repository
gitnexus analyze --force          # Force full re-index
gitnexus list                     # Show indexed repos
gitnexus serve                    # Start web UI (http://localhost:port)
```

---

## 4. Java/Gradle Project Integration

**Full Java Support:**
- Imports, named bindings, exports, type annotations
- Constructor inference
- Framework detection for common Java frameworks
- Standard project structure handling

**Setup for Gradle Projects:**
1. Run from project root: `npx gitnexus analyze`
2. Tool automatically detects and indexes Gradle structure
3. No manual configuration needed for standard layouts

---

## 5. Configuration Options

**Index Time Options:**
- `--force`: Bypass cache, full re-indexing
- `[path]`: Specify custom repository path

**Graph Features (auto-enabled):**
- Symbol clustering (functional communities)
- Call chain analysis
- Type relationship mapping
- Import resolution
- Breaking change detection

**Output Artifacts:**
- `.gitnexus/` index cache
- `AGENTS.md` — contextual information for AI agents
- `CLAUDE.md` — Claude Code integration guide

---

## 6. Architecture & Approach

### Design Philosophy
Departures from traditional graph RAG:
- **Precomputed Analysis**: Clustering, tracing, scoring happens at index time
- **Complete Context**: Single queries deliver full context (vs. multi-step LLM exploration)
- **Language-Aware**: Uses Tree-sitter for accurate structural parsing per language

### MCP Tools Available
Once configured, AI agents access 7 tools including:
- Impact analysis (understand change scope)
- Context exploration (navigate relationships)
- Change detection (identify breaking changes)
- Multi-file renaming
- Functional community discovery

---

## Summary

| Aspect | Details |
|--------|---------|
| **Type** | Code indexing + knowledge graph for AI agents |
| **Setup Time** | <5 minutes (single command) |
| **Languages** | TypeScript, JavaScript, Python, Java, others |
| **Editors** | Claude Code, Cursor, Codex, Windsurf |
| **Cost** | Open source, CLI free |
| **Best For** | Teams using AI-assisted coding; medium-large codebases |

---

## Unresolved Questions
None — all requested information is available in the official documentation.
