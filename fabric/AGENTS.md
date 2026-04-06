# AGENTS

## OVERVIEW

`fabric` 只负责 Fabric 侧入口、Fabric API 注册和 Fabric 清单资源。共享行为继续落在 `common`，这里主要做接线。

## WHERE TO LOOK

| 任务 | 位置 | 备注 |
|---|---|---|
| 主入口 | `src/main/java/com/euphony/better_client/fabric/BetterClientFabric.java` | 只调 `BetterClient.init()` |
| 客户端入口 | `src/main/java/com/euphony/better_client/fabric/client/BetterClientFabricClient.java` | 调 `BetterClientFabricBootstrap.initClient()` |
| 客户端注册 | `src/main/java/com/euphony/better_client/fabric/client/BetterClientFabricBootstrap.java` | HUD、命令、按键、消息、屏幕事件 |
| 平台实现 | `src/main/java/com/euphony/better_client/fabric/platform` | Fabric 平台 helper |
| Mod Menu | `src/main/java/com/euphony/better_client/fabric/integration/ModMenuIntegration.java` | 可选集成 |
| 模组清单 | `src/main/resources/fabric.mod.json` | entrypoints、depends、accessWidener |

## CONVENTIONS

- Fabric 侧注册集中在 `BetterClientFabricBootstrap`，优先沿用现有“拆分成 `registerXxx()` 私有静态方法”的组织方式。
- HUD、tooltip、客户端命令、屏幕事件都通过 Fabric API 的显式静态注册完成；不要把这些注册塞回 `common`。
- `fabric.mod.json` 是 entrypoint 与依赖声明的单一事实来源；新增入口或集成时同步更新它。
- `sourceSets.main.resources` 已并入 `common/src/generated/resources`；资源生成仍从 NeoForge datagen 侧发起。

## ANTI-PATTERNS

- 不要在 Fabric 目录里复制 `common` 逻辑；这里只做 Fabric 接线。
- 不要直接写全限定类型名；例如 `Component`、`ClientLevel` 先 import。
- 不要在这里引入 NeoForge 事件总线模式；Fabric 侧保持显式 API 注册。
- 不要忘记 `fabric.mod.json` 里的 `main` / `client` / `modmenu` entrypoint 一致性。

## COMMANDS

```powershell
# 继承根 AGENTS 里的 Java 25 / root build 规则
.\gradlew :fabric:build
```

## NOTES

- `fabric.mod.json` 当前同时声明 `main`、`client`、`modmenu` 三类入口。
- Fabric 这里只有少量 Java 文件；优先保持薄封装，不在此堆业务分支。
