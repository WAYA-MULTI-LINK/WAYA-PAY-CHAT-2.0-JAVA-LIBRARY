# WayaPay artifact

Pre-built JARs for the WayaPay Java SDK, committed to the repo so they ship via GitHub.
Use this to consume the library without publishing to Maven Central.

Each release lives in its own `version<x.y.z>/` folder. The current release is
[`version2.0.0/`](version2.0.0/):

| File | What it is |
|------|------------|
| `wayapay-java-sdk-2.0.0.zip` | **Download this.** Bundles the three files below. |
| `wayapay-java-sdk-2.0.0.jar` | The compiled library — put this on your classpath. |
| `wayapay-java-sdk-2.0.0-sources.jar` | Sources, for IDE navigation and Javadoc. |
| `wayapay-java-sdk-2.0.0.pom` | The POM, so the one runtime dependency (Jackson) resolves transitively. |

## Rebuild the artifact

From the repo root (the `VERSION` matches `<version>` in `pom.xml`):

```bash
VERSION=2.0.0
mvn -q clean package
mkdir -p artifact/version$VERSION
cp target/wayapay-java-sdk-$VERSION.jar target/wayapay-java-sdk-$VERSION-sources.jar artifact/version$VERSION/
cp pom.xml artifact/version$VERSION/wayapay-java-sdk-$VERSION.pom
(cd artifact/version$VERSION && zip -q wayapay-java-sdk-$VERSION.zip wayapay-java-sdk-$VERSION.jar wayapay-java-sdk-$VERSION-sources.jar wayapay-java-sdk-$VERSION.pom)
```

## Consume it

### Option 1 — Download the zip, install into your local Maven repo (recommended)

Download `version2.0.0/wayapay-java-sdk-2.0.0.zip` (open it on GitHub → **Download raw file**, or
`curl` the raw URL), unzip it, then from the unzipped folder:

```bash
unzip wayapay-java-sdk-2.0.0.zip
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
