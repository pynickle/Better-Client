# AGENTS

**Generated:** 2026-04-04  
**Commit:** `86d87c4`  
**Branch:** `master`

## OVERVIEW

Better Client 是基于 Architectury 的多加载器客户端模组仓库。`common` 放共享逻辑，`fabric` / `neoforge` 只放各自加载器的引导、平台接线与清单资源。

## STRUCTURE

```text
./
├── common/      # 共享代码、mixin、access widener、资源
├── fabric/      # Fabric 入口、Fabric API 注册、fabric.mod.json
├── neoforge/    # NeoForge 入口、事件订阅、datagen、mods.toml
├── .github/     # release workflow
├── build.gradle # 根构建，统一 Java release=25
└── package.json # semantic-release 依赖
```

## WHERE TO LOOK

| 任务               | 位置                                                                                              | 备注                                        |
|------------------|-------------------------------------------------------------------------------------------------|-------------------------------------------|
| 共享初始化            | `common/src/main/java/com/euphony/better_client/BetterClient.java`                              | 平台无关入口                                    |
| Mixin 列表         | `common/src/main/resources/better_client.mixins.json`                                           | client/common mixin 都在这里登记                |
| Access widener   | `common/src/main/resources/better_client.accesswidener`                                         | 优先走 widener / accessor，不要反射               |
| 共享平台抽象           | `common/src/main/java/com/euphony/better_client/platform`                                       | 通过 `Platform` / `PlatformServices` 间接访问   |
| Fabric 入口        | `fabric/src/main/resources/fabric.mod.json`                                                     | `main` / `client` / `modmenu` entrypoints |
| Fabric 客户端接线     | `fabric/src/main/java/com/euphony/better_client/fabric/client/BetterClientFabricBootstrap.java` | 直接注册 HUD、命令、事件                            |
| NeoForge 入口      | `neoforge/src/main/java/com/euphony/better_client/neoforge/BetterClientNeoForge.java`           | `@Mod` 主入口                                |
| NeoForge 客户端接线   | `neoforge/src/main/java/com/euphony/better_client/neoforge/client/BCClientNeoforge.java`        | `@EventBusSubscriber` 事件注册                |
| NeoForge datagen | `neoforge/src/main/java/com/euphony/better_client/neoforge/datagen/DataGenerators.java`         | 生成 common 资源                              |
| 发布流程             | `.github/workflows/release.yml` / `.releaserc.json`                                             | Java 25 + pnpm + semantic-release         |

## CONVENTIONS

- 统一通过根 Gradle 构建；根 `build.gradle` 把 `JavaCompile.options.release` 固定为 `25`。
- 本地编译使用 Java 25；路径固定为 `D:\Program Files\Eclipse Adoptium\graalvm-jdk-25.0.1+8.1`。
- 处理 Minecraft 模组功能改动前，先读取并遵循 `.agents/skills/minecraft-source-cache/SKILL.md`；优先复用现有 source cache / Gradle 产物，再决定是否补缓存。
- 修改共享逻辑时，优先留在 `common`；只有加载器 API / 清单 / 事件总线差异才放 `fabric` 或 `neoforge`。
- `common` 通过 `ServiceLoader` + `BetterClientPlatform` 做平台分发；不要从共享代码直接依赖 Fabric / NeoForge API。
- NeoForge datagen 输出到 `common/src/generated/resources`，Fabric / NeoForge 构建都会把该目录并入资源。

## ANTI-PATTERNS

- 禁止使用基于反射的运行时调用来绕过类型系统，包括但不限于 `Class.forName(...)`、`Method.invoke(...)`、`.class.getMethods()`、`getDeclaredConstructors()`。
- 禁止把业务对象降级为 `Object` 后再做反射调用、成员读写、业务分发或转调。Mixin 场景下为访问已知目标类型而进行的受控强转/过桥（如 `(Object)` 后再转回具体 Mixin 类型）可以保留；其余场景优先使用明确具体类型、受控接口或 accessor/mixin 暴露出的强类型 API。
- 修改 Minecraft / mod 交互逻辑时，先确认当前映射下的真实签名，再按强类型方式接入；不要为了“兼容未知版本”引入反射兜底。
- 不要在代码里直接写 `net.minecraft.client.multiplayer.ClientLevel` 这种内联全限定名；先 `import`，再使用简单类名。
- 不要把仅属于单一加载器的 API 混进 `common`。
- 不要把 `compileJava` 当作交付验证命令；这个仓库要求跑 `gradlew build`。

## UNIQUE STYLES

- Mixin / `@Unique` / accessor 方法统一使用 `better_client$` 前缀。
- accessor 放在 `com.euphony.better_client.mixin.accessor`；字段暴露优先 `@Accessor`，不要手搓反射桥。
- Fabric 侧偏向显式静态注册；NeoForge 侧偏向 `@SubscribeEvent` + 事件驱动注册。

## COMMANDS

```powershell
$env:JAVA_HOME = 'D:\Program Files\Eclipse Adoptium\graalvm-jdk-25.0.1+8.1'
.\gradlew --no-daemon build
.\gradlew --no-daemon :fabric:build
.\gradlew --no-daemon :neoforge:build
pnpm install
npx semantic-release
```

## NOTES

- 当前 CI release workflow 也固定使用 Java 25。
- 仓库未配置常规单元测试；默认验证是 `gradlew --no-daemon build`。
- `fabric.mod.json` 里的 Java 依赖范围仍写 `>=21`，但实际编译目标已经提升到 Java 25；改动构建相关内容时以 Gradle 配置为准。
