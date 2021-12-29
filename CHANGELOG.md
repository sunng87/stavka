# Change Log

## [0.7.0]

### Added

- Added global config accessor functions `$$`/`$$l`/`$$f`/`$$s`/`$$b`

### Changed

- Removed global config access capability from `$` function family
- `$l`/`$f`/`$b` now returns nil when default value not provided,
  instead of 0/0/false

## [0.6.0]

### Changed

- Made clj-yaml/cheshire/hawk/clj-http optional dependencies. Features
  depend on these libraries are only activated when they are found on classpath.

## [0.5.1]

### Added

- Support for env prefix

### Changed

- Fixed build on Java 11

## [0.4.1]

### Added

- Added support for EDN.
- dict resolver now tries to lookup both string key and keyword key

## [0.4.0]

Initial release

[0.6.0]: https://github.com/your-name/stavka/compare/0.5.1...0.6.0
[0.5.1]: https://github.com/your-name/stavka/compare/0.4.1...0.5.1
[0.4.1]: https://github.com/your-name/stavka/compare/0.4.0...0.4.1
