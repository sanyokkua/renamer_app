# Skills for Renamer App Development

This directory contains skill definitions that have been installed as Claude Code skills to ensure consistent, high-quality code, tests, and
documentation.

## Installed Skills

The following skills have been installed in `~/.claude/skills/`:

### 1. java-developer

**Purpose**: Write high-quality Java code following modern best practices (Java 21-25)

**Key Principles**:

- Clear, not clever code
- Immutability by default
- Constructor injection (never field injection)
- Records for DTOs
- Proper Javadoc (what/why, not how)
- No marketing language in documentation
- Never Use RAW objects

**Usage**: Automatically applies when writing Java code. Enforces:

- SOLID principles
- Modern Java features (Records, var, pattern matching, text blocks)
- Proper null safety with Optional
- Clean exception handling
- Meaningful names and short methods

### 2. junit5-test

**Purpose**: Write comprehensive JUnit 5 tests with proper mocking strategy

**Key Principles**:

- Test behavior, not implementation
- Mock only external boundaries (repositories, APIs, file system)
- Use real objects for DTOs and domain models
- Clear Given-When-Then structure
- AssertJ for readable assertions
- Parameterized tests for multiple inputs

**Usage**: Automatically applies when writing tests. Enforces:

- Descriptive test names (shouldXxx_whenYyy or given_when_then)
- No testing of logging or mock stubs
- Proper exception testing with assertThrows
- Edge case coverage (nulls, empties, boundaries)

### 3. mermaid-diagram

**Purpose**: Create syntactically correct Mermaid diagrams that render without errors

**Key Principles**:

- **NEVER use inline comments** (causes parse errors)
- Quote all text with special characters
- Use alphanumeric IDs only
- Avoid reserved keywords as IDs
- Escape parentheses and brackets in labels

**Usage**: Automatically applies when creating diagrams. Enforces:

- Comments only on separate lines
- Proper text escaping with HTML entities
- Clear node IDs vs labels
- Appropriate diagram types for use cases

## When These Skills Apply

These skills are **automatically applied** by Claude Code whenever you:

1. **Write Java code** → java-developer skill activates
2. **Write JUnit tests** → junit5-test skill activates
3. **Create Mermaid diagrams** → mermaid-diagram skill activates

## Skill Enforcement Rules

All three skills enforce critical rules that must be followed:

### Critical Rules (Never Break)

From **java-developer**:

- ❌ Never use field injection
- ❌ Never return null (use Optional or empty collections)
- ❌ Never use marketing language in Javadoc
- ❌ Never document implementation details

From **junit5-test**:

- ❌ Never mock value objects or DTOs
- ❌ Never test that mocks return stubbed values
- ❌ Never write tests without meaningful assertions
- ❌ Never test private methods directly

From **mermaid-diagram**:

- ❌ **NEVER put comments inline** after diagram statements
- ❌ Never use parentheses in labels without escaping
- ❌ Never start IDs with numbers
- ❌ Never use reserved keywords as IDs

## Project-Specific Adaptations

These skills have been adapted for the Renamer App project:

### Java Development

- Use Google Guice for DI with `@RequiredArgsConstructor(onConstructor_ = {@Inject})`
- Follow Command Pattern for all file operations
- Keep business logic in core module (no UI dependencies)
- Use FilesOperations for all file system interactions

### Testing

- Mirror production package structure in tests
- Use `@ExtendWith(MockitoExtension.class)` for unit tests
- Mock only: repositories, external services, FilesOperations
- Use real FileInformation, RenameModel, and domain objects
- Test commands through public execute() method

### Documentation

- Include Mermaid diagrams for architecture documentation
- Follow the patterns in docs/ARCHITECTURE.md
- Update CLAUDE.md when adding new patterns or features

## Updating Skills

To update a skill:

1. Edit the source file in `docs/skills/`
2. Regenerate the installed skill:
   ```bash
   cp docs/skills/JavaDeveloperSkill.md ~/.claude/skills/java-developer.md
   cp docs/skills/JUnit5TestSkill.md ~/.claude/skills/junit5-test.md
   cp docs/skills/MermaidDiagramSkill.md ~/.claude/skills/mermaid-diagram.md
   ```

## Verification

To verify skills are properly installed:

```bash
ls -lh ~/.claude/skills/
```

You should see:

- `java-developer.md` (~6KB)
- `junit5-test.md` (~9KB)
- `mermaid-diagram.md` (~6KB)

## Additional Notes

These skills work together to ensure:

- **Code quality**: Clean, maintainable Java following modern practices
- **Test coverage**: Comprehensive tests that verify behavior, not implementation
- **Documentation clarity**: Clear diagrams and Javadoc that explain what and why

When working on this project, trust that these skills will guide you to write code that follows the established patterns and quality standards.
