# AGENTS

## OVERVIEW

`neoforge` 只负责 NeoForge 侧入口、事件订阅、访问转换器与 datagen。共享功能实现仍在 `common`，这里处理 NeoForge 生命周期接线。

## WHERE TO LOOK

| 任务 | 位置 | 备注 |
|---|---|---|
| 主入口 | `src/main/java/com/euphony/better_client/neoforge/BetterClientNeoForge.java` | `@Mod(BetterClient.MOD_ID)` |
| 客户端事件接线 | `src/main/java/com/euphony/better_client/neoforge/client/BCClientNeoforge.java` | `@EventBusSubscriber` + `@SubscribeEvent` |
| 平台实现 | `src/main/java/com/euphony/better_client/neoforge/platform` | NeoForge 平台 helper |
| Datagen | `src/main/java/com/euphony/better_client/neoforge/datagen` | `GatherDataEvent.Client` 注册 provider |
| 模组清单 | `src/main/resources/META-INF/neoforge.mods.toml` | 依赖、mixins、AT |
| Access transformer | `src/main/resources/META-INF/accesstransformer.cfg` | NeoForge 访问控制 |

## CONVENTIONS

- NeoForge 侧注册统一走事件总线；保持 `@EventBusSubscriber` + `@SubscribeEvent` 风格，不要改成 Fabric 式手动注册。
- GUI layer、item model property、按键、客户端命令、tooltip、screen/key 事件都集中在 `BCClientNeoforge`。
- datagen 入口放在 `neoforge/datagen`，生成结果写回 `common/src/generated/resources`。
- `neoforge.mods.toml` 是 NeoForge 资源声明单一事实来源；mixins、依赖范围、AT 路径都以它为准。

## ANTI-PATTERNS

- 不要把共享业务逻辑复制到 NeoForge 目录；这里应保持 loader glue。
- 不要直接写内联全限定名；Minecraft / NeoForge 类型先 import。
- 不要绕过事件总线去散落注册逻辑；新增客户端接线优先贴近 `BCClientNeoforge` 现有结构。
- 不要忘记 datagen 与 `common/src/generated/resources` 的联动。

## COMMANDS

```powershell
$env:JAVA_HOME = 'D:\Program Files\Eclipse Adoptium\graalvm-jdk-25.0.1+8.1'
.\gradlew :neoforge:build
.\gradlew runData
```

## NOTES

- NeoForge 侧同时使用 `neoforge.mods.toml` 和 `META-INF/accesstransformer.cfg`。
- `BCClientNeoforge` 已覆盖大多数客户端事件接线；新增事件优先并入既有类，除非职责明显独立。
