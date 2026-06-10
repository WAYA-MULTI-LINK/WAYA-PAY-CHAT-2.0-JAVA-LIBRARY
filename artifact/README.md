# WayaPay artifact

Pre-built JARs for the WayaPay Java SDK, committed to the repo so they ship via GitHub.
Use this to consume the library without publishing to Maven Central.

Files in this folder:

| File | What it is |
|------|------------|
| `wayapay-java-sdk-2.0.0.jar` | The compiled library — put this on your classpath. |
| `wayapay-java-sdk-2.0.0-sources.jar` | Sources, for IDE navigation and Javadoc. |
| `wayapay-java-sdk-2.0.0.pom` | The POM, so the one runtime dependency (Jackson) resolves transitively. |

## Rebuild the JARs

From the repo root:

```bash
mvn -q clean package
cp target/wayapay-java-sdk-*.jar artifact/
cp pom.xml artifact/wayapay-java-sdk-2.0.0.pom
# version comes from <version> in pom.xml
```

## Consume it

### Option 1 — Download from GitHub, install into your local Maven repo (recommended)

Download `wayapay-java-sdk-2.0.0.jar` and `wayapay-java-sdk-2.0.0.pom` (open the file on GitHub and
click **Download raw file**, or `curl` the raw URL), then:

```bash
mvn install:install-file \
  -Dfile=wayapay-java-sdk-2.0.0.jar \
  -DpomFile=wayapay-java-sdk-2.0.0.pom \
  -Dsources=wayapay-java-sdk-2.0.0-sources.jar
```

Now depend on it like any other library — Jackson is pulled in transitively from the POM:

```xml
<dependency>
    <groupId>com.waya</groupId>
    <artifactId>wayapay-java-sdk</artifactId>
    <version>2.0.0</version>
</dependency>
```

### Option 2 — System-scope dependency (no install step)

Point Maven straight at the JAR file. Note: with `system` scope you must also add Jackson yourself.

```xml
<dependency>
    <groupId>com.waya</groupId>
    <artifactId>wayapay-java-sdk</artifactId>
    <version>2.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/wayapay-java-sdk-2.0.0.jar</systemPath>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.17.1</version>
</dependency>
```

### Option 3 — Gradle flat-dir

Drop the JAR into a `libs/` folder, then:

```groovy
repositories {
    flatDir { dirs 'libs' }
    mavenCentral() // for Jackson
}

dependencies {
    implementation name: 'wayapay-java-sdk-2.0.0'
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.17.1'
}
```

### Option 4 — Plain classpath

For a non-Maven/Gradle project, put the JAR plus a Jackson `jackson-databind` (and its
`jackson-core` / `jackson-annotations`) on the classpath:

```bash
javac -cp "wayapay-java-sdk-2.0.0.jar:jackson-databind-2.17.1.jar:..." MyApp.java
```

The library targets **Java 17+** and has a single runtime dependency: Jackson (`jackson-databind`).
