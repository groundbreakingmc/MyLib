# MyLib [![Java](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://jdk.java.net/17/)

> ✨ A lightweight and extensible Java library tailored for Minecraft plugin development.  
> Includes utilities for databases, tasks, commands, actions, and more.

---

## Features

- ⚙️ Abstract Database layer with HikariCP support
- ⏱️ Tick-based task system with virtual thread support
- 🧩 Context-aware action system (`Action<C extends ActionContext>`)
- 📦 Fully modular — use only what you need: MyLib won’t load optional systems (like database, config, Vault, LuckPerms) unless your plugin uses them directly.

### 🔧 Plugin development utilities

- 🛠️ Simplified config loading using Bukkit & Configurate (with auto-updating support)
- 🔌 Easy provider access for Vault (Chat, Permissions, Economy) and LuckPerms
- 🧑‍🤝‍🧑 Utilities for LuckPerms: permission checks, group access, metadata
- 📑 Modern event registration system with cleaner lifecycle binding
- ⏳ Expiring collections (`ExpiringMap`, etc.), Pair, Triplet, and other helpers
- 🎨 Advanced colorizers for strings and MiniMessage/Adventure components
- 📍 Region-aware event utilities for WorldGuard (e.g. player enter/leave zone triggers)
- 📋 PersistentDataContainer helpers for blocks
- 👀 Enhanced vanish detection (supports multiple plugins, visibility API)

### 🛠️ Tools for maintainability

- 📂 Asynchronous file logger + enhanced console logger with formatting
- 🔄 Update checker with styled log output and optional jar auto-download
- 🧰 Tons of tiny utilities for formatting, validation, reflection, and more

---

## Getting Started

### 📦 Installation

MyLib is available via [JitPack](https://jitpack.io).

#### ⚙️ Step 1: Add JitPack Repository

<details>
<summary><strong>Maven</strong></summary>

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
</details>

<details>
<summary><strong>Gradle (Groovy)</strong></summary>

```groovy
maven { url 'https://jitpack.io' }
```
</details>

<details>
<summary><strong>Gradle (Kotlin DSL)</strong></summary>

```kotlin
maven("https://jitpack.io")
```
</details>

---

#### ⚙️ Step 2: Add the Dependency

Replace `VERSION` with the latest release/tag, `commit-hash` or `main-SNAPSHOT` to be always up to date.

<details>
<summary><strong>Maven</strong></summary>

```xml
<dependency>
    <groupId>com.github.groundbreakingmc</groupId>
    <artifactId>MyLib</artifactId>
    <version>VERSION</version>
    <scope>provided</scope>
</dependency>
```
</details>

<details>
<summary><strong>Gradle (Groovy)</strong></summary>

```groovy
implementation 'com.github.groundbreakingmc:MyLib:VERSION'
```
</details>

<details>
<summary><strong>Gradle (Kotlin DSL)</strong></summary>

```kotlin
implementation("com.github.groundbreakingmc:MyLib:VERSION")
```
</details>

---

### 🧩 Shading MyLib (Recommended for plugins)

To avoid classpath conflicts and make your plugin standalone, it's recommended to **shade** MyLib.

<details>
<summary><strong>Maven</strong></summary>

```xml

<project>
    <build>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.6.0</version>
            <configuration>
                <relocations>
                    <relocation>
                        <pattern>org.bstats</pattern>
                        <shadedPattern>com.github.groundbreakingmc.newbieguard.metrics</shadedPattern>
                    </relocation>
                </relocations>
            </configuration>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <createDependencyReducedPom>false</createDependencyReducedPom>
                        <minimizeJar>true</minimizeJar>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </build>

    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>com.github.groundbreakingmc</groupId>
            <artifactId>MyLib</artifactId>
            <version>VERSION</version>
            <scope>compile</scope>
        </dependency>
    </dependencies>
</project>

```
</details>

<details>
<summary><strong>Gradle (Groovy)</strong></summary>

```groovy
plugins {
    id 'io.github.goooler.shadow' version '8.1.8'
}

dependencies {
    implementation 'com.github.groundbreakingmc:MyLib:VERSION'
}

shadowJar {
    relocate 'com.github.groundbreakingmc.mylib', 'your.package.name.mylib'
}
```
</details>

<details>
<summary><strong>Gradle (Kotlin DSL)</strong></summary>

```kotlin
plugins {
    id("io.github.goooler.shadow") version "8.1.8"
}

dependencies {
    implementation("com.github.groundbreakingmc:MyLib:VERSION")
}

tasks {
    shadowJar {
        relocate("com.github.groundbreakingmc.mylib", "our.package.name.mylib")
        minimize()
    }

    build {
        dependsOn(shadowJar)
    }
}
```
</details>

**Don't forget** to fill in plugin.yml if you use the following features
```yaml
softdepend:
  - WorldGuard      # For WorldGuard utils, entry/leave region events
  - LuckPerms       # For LuckPerms utils
  - Vault           # For Vault Provider utils
  - UltimateVanish  # Used in vanish detection
  - SuperVanish     # Used in vanish detection
  - PremiumVanish   # Used in vanish detection

libraries:
  # For ExpiringMap and ExpiringSet
  - com.github.ben-manes.caffeine:caffeine:3.1.8
  # For ConfigurateLoader, ConfigProcessor, ConfigUtil
  - org.spongepowered:configurate-yaml:4.0.0
  # For Database
  - com.zaxxer:HikariCP:5.1.0
```

---

## Contributing

Pull requests are welcome! Please follow the existing code style and add tests where applicable.
