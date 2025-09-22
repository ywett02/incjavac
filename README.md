# incjavac - Incremental Java Compiler CLI

`incjavac` is a command-line tool that demonstrates **incremental compilation in Java**. Instead of recompiling an
entire codebase, it recompiles only the files that changed and their dependencies, making iterative builds faster. This
is an **educational proof of concept**, not a production-ready compiler.

## Main Features

- **Incremental compilation**: recompiles only changed files and their dependencies
- **Dependency tracking**: analyzes and tracks class dependencies using ASM bytecode analysis
- **Caching**: stores compilation metadata for subsequent compilations (in JSON format for readability)
- **Stale data removal**: automatically cleans up outputs when their sources are removed

## How It Works

- **First run**
    - Compiles all Java files in the source directory
    - Builds dependency maps and stores compilation metadata
- **Subsequent Runs**
    - Detects changed files using file digests (MD5 hashes)
  - Builds a "dirty set" including changed sources files with their direct and transitive superclass dependencies
    - Recompiles only the dirty set, leaving unchanged files untouched
    - Updates dependency maps and metadata for next compilation

## Current Limitations

- **No incremental support for library (classpath) changes** - any modification to the classpath results in a full
  recompilation
- **No distinction between ABI and non-ABI changes** - both types of source changes currently trigger recompilation

## Usage

### Command Line Arguments

| Argument  | Required | Description                                      |
|-----------|----------|--------------------------------------------------|
| `-src`    | **Yes**  | Source directory with Java files                 |
| `-cp`     | No       | Classpath (JARs, ZIPs, directories)              |
| `-d`      | No       | Output directory (default: `$src/build/classes`) |
| `-cd`     | No       | Cache directory (default: `$src/build/cache`)    |
| `--debug` | No       | Enable debug output (default: `false`)           |

### Running with Gradle

```bash
# Basic usage
./gradlew run --args="-src example/basic/src"

# With dependencies
./gradlew run --args="-src example/dependencies/src -cp example/dependencies/lib/classes:example/dependencies/lib/time.jar:example/dependencies/lib/formatter.zip"

# Full example
./gradlew run --args="-src example/dependencies/src -cp example/dependencies/lib/classes:example/dependencies/lib/time.jar:example/dependencies/lib/formatter.zip -d example/dependencies/build/classes -cd example/dependencies/build/cache --debug"
```

### Installation

```bash
# Install to system path
./gradlew install
export PATH="$PWD/build/install/incjavac/bin:$PATH"
incjavac -src example/basic/src
```
