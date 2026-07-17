# WayaQuick artifact

Pre-built JARs for the WayaQuick Java SDK, committed to the repo so they ship via GitHub.
Use this to consume the library without publishing to Maven Central.

Each release lives in its own `version<x.y.z>/` folder. The current release is
[`version2.0.1/`](version2.0.1/):

| File | What it is |
|------|------------|
| `wayaquick-integration-2.0.1.zip` | **Download this.** Bundles the three files below. |
| `wayaquick-integration-2.0.1.jar` | The compiled library — put this on your classpath. |
| `wayaquick-integration-2.0.1-sources.jar` | Sources, for IDE navigation and Javadoc. |
| `wayaquick-integration-2.0.1.pom` | The POM, so the one runtime dependency (Jackson) resolves transitively. |

## Rebuild the artifact

From the repo root (the `VERSION` matches `<version>` in `pom.xml`):

```bash
VERSION=2.0.1
mvn -q clean package
mkdir -p artifact/version$VERSION
cp target/wayaquick-integration-$VERSION.jar target/wayaquick-integration-$VERSION-sources.jar artifact/version$VERSION/
cp pom.xml artifact/version$VERSION/wayaquick-integration-$VERSION.pom
(cd artifact/version$VERSION && zip -q wayaquick-integration-$VERSION.zip wayaquick-integration-$VERSION.jar wayaquick-integration-$VERSION-sources.jar wayaquick-integration-$VERSION.pom)
```

## Consume it

### Option 1 — Download the zip, install into your local Maven repo (recommended)

Download `version2.0.1/wayaquick-integration-2.0.1.zip` (open it on GitHub → **Download raw file**, or
`curl` the raw URL), unzip it, then from the unzipped folder:

```bash
unzip wayaquick-integration-2.0.1.zip
mvn install:install-file \
  -Dfile=wayaquick-integration-2.0.1.jar \
  -DpomFile=wayaquick-integration-2.0.1.pom \
  -Dsources=wayaquick-integration-2.0.1-sources.jar
```

Now depend on it like any other library — Jackson is pulled in transitively from the POM:

```xml
<dependency>
    <groupId>io.github.waya-multi-link</groupId>
    <artifactId>wayaquick-integration</artifactId>
    <version>2.0.1</version>
</dependency>
```

### Option 2 — System-scope dependency (no install step)

Point Maven straight at the JAR file. Note: with `system` scope you must also add Jackson yourself.

```xml
<dependency>
    <groupId>io.github.waya-multi-link</groupId>
    <artifactId>wayaquick-integration</artifactId>
    <version>2.0.1</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/wayaquick-integration-2.0.1.jar</systemPath>
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
    implementation name: 'wayaquick-integration-2.0.1'
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.17.1'
}
```

### Option 4 — Plain classpath

For a non-Maven/Gradle project, put the JAR plus a Jackson `jackson-databind` (and its
`jackson-core` / `jackson-annotations`) on the classpath:

```bash
javac -cp "wayaquick-integration-2.0.1.jar:jackson-databind-2.17.1.jar:..." MyApp.java
```

The library targets **Java 17+** and has a single runtime dependency: Jackson (`jackson-databind`).
