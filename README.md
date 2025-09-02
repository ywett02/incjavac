# incjavac - Incremental Java Compiler CLI

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
