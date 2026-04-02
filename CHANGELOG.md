# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2026-04-02

### Added

- **DAG-based feature dependency resolution**: Automatically builds a Directed Acyclic Graph from feature dependencies based on method parameter names
- **Parallel computation**: Executes independent features concurrently using a configurable thread pool
- **Annotation-driven configuration**: Simple `@Feature` and `@FeatureClass` annotations for defining feature functions
- **Spring Boot auto-configuration**: Seamless integration with Spring Boot applications via starter dependency
- **Cycle detection**: Detects and reports circular dependencies in feature definitions to prevent infinite loops
- **Result caching**: Caches computed feature values to eliminate redundant calculations
- **Custom feature beans**: Support for programmatic feature definition via `AbstractFeatureBean`
- **Feature bean post-processor**: Extensible post-processing mechanism similar to Spring's `BeanPostProcessor`
- **Configurable thread pool**: Customizable core and maximum thread pool sizes
- **Calculation timeout**: Configurable timeout for feature calculations
- **Debug mode**: Optional debug mode for troubleshooting feature calculations

### Technical Details

- Minimum Java version: JDK 8 (with `-parameters` compiler flag)
- Spring Boot 2.x compatible
- Lightweight with minimal dependencies

---

## Version History

| Version | Release Date | Highlights |
|---------|--------------|------------|
| 1.0.0   | 2026-04-02   | Initial release with core DAG engine |

[Unreleased]: https://github.com/[USERNAME]/feature/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/[USERNAME]/feature/releases/tag/v1.0.0
