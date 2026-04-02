# Contributing to Feature Engine

Thank you for your interest in contributing to Feature Engine! This document provides guidelines and information for contributors.

## Table of Contents

- [Development Environment](#development-environment)
- [Development Workflow](#development-workflow)
- [Code Standards](#code-standards)
- [Pull Request Guidelines](#pull-request-guidelines)
- [Issue Guidelines](#issue-guidelines)

## Development Environment

### Prerequisites

- **JDK 8** (required)
  - The `-parameters` compiler argument **MUST** be enabled
  - This is essential for the annotation processor to correctly resolve method parameter names
- **Maven 3.6+**
- **Git**

### Maven Compiler Configuration

Ensure your `pom.xml` includes the following compiler configuration:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.8.1</version>
    <configuration>
        <source>8</source>
        <target>8</target>
        <encoding>UTF-8</encoding>
        <compilerArgs>
            <arg>-parameters</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

### Building the Project

```bash
# Clone the repository
git clone https://github.com/[YOUR_USERNAME]/feature.git
cd feature

# Build the project
mvn clean install

# Run tests
mvn test
```

## Development Workflow

We follow the standard GitHub Flow:

1. **Fork** the repository to your GitHub account
2. **Clone** your fork locally
3. **Create a branch** for your feature or bugfix
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/your-bugfix-name
   ```
4. **Write code** and tests
5. **Commit** your changes with clear, descriptive messages
6. **Push** to your fork
7. **Create a Pull Request** against the `main` branch

### Branch Naming Convention

- `feature/` - New features
- `fix/` - Bug fixes
- `docs/` - Documentation updates
- `refactor/` - Code refactoring
- `test/` - Test additions or modifications

## Code Standards

### Naming Conventions

- **Classes**: PascalCase (e.g., `FeatureEngine`, `AbstractFeatureBean`)
- **Methods**: camelCase (e.g., `calcFeature`, `getDependencies`)
- **Variables**: camelCase (e.g., `featureBean`, `calcTimeout`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `DEFAULT_TIMEOUT`, `MAX_POOL_SIZE`)
- **Packages**: lowercase (e.g., `com.github.zh.engine.processor`)

### Comments

- All comments **MUST** be written in **English**
- Use clear and concise language
- Avoid obvious comments that don't add value

### JavaDoc Requirements

All public classes and methods must have JavaDoc comments:

```java
/**
 * Calculates the specified features based on the given input data.
 *
 * @param originDataMap the input data map containing initial values
 * @param calcFeatures the set of feature names to calculate
 * @return a map containing the calculated feature values
 * @throws CalculateException if a calculation error occurs
 */
public Map<String, Object> calc(Map<String, Object> originDataMap, Set<String> calcFeatures) {
    // implementation
}
```

### Code Style

- Use 4 spaces for indentation (no tabs)
- Maximum line length: 120 characters
- Always use braces for control statements, even single-line blocks
- One statement per line

## Pull Request Guidelines

### Before Submitting

1. Ensure all tests pass: `mvn test`
2. Ensure the code compiles without warnings: `mvn clean compile`
3. Update documentation if necessary
4. Add tests for new functionality

### PR Description Template

```markdown
## Description
Brief description of the changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Related Issue
Fixes #(issue number)

## Checklist
- [ ] My code follows the project's code standards
- [ ] I have added tests that prove my fix/feature works
- [ ] All new and existing tests pass
- [ ] I have updated the documentation accordingly
- [ ] My changes generate no new warnings
```

### Review Process

1. All PRs require at least one approval before merging
2. Address all review comments
3. Keep PRs focused and reasonably sized
4. Squash commits if requested

## Issue Guidelines

### Bug Reports

When reporting a bug, please include:

- **Title**: Clear, concise description of the bug
- **Environment**: JDK version, Maven version, OS
- **Steps to Reproduce**: Detailed steps to reproduce the issue
- **Expected Behavior**: What you expected to happen
- **Actual Behavior**: What actually happened
- **Code Sample**: Minimal code to reproduce the issue (if applicable)
- **Stack Trace**: Full stack trace (if applicable)

### Feature Requests

When requesting a feature, please include:

- **Title**: Clear description of the feature
- **Use Case**: Why this feature would be useful
- **Proposed Solution**: Your idea for implementation (optional)
- **Alternatives Considered**: Other approaches you've considered

### Issue Labels

- `bug` - Something isn't working
- `enhancement` - New feature or request
- `documentation` - Documentation improvements
- `good first issue` - Good for newcomers
- `help wanted` - Extra attention is needed
- `question` - Further information is requested

## License

By contributing to Feature Engine, you agree that your contributions will be licensed under the [AGPL v3 License](LICENSE).

## Questions?

If you have questions about contributing, feel free to open an issue with the `question` label.

Thank you for contributing to Feature Engine!
