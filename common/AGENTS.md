# AGENTS

## OVERVIEW

`common` 只放共享逻辑：客户端功能、配置界面、mixin、平台抽象、资源与通用工具。这里的代码必须同时服务 Fabric 与 NeoForge。

## WHERE TO LOOK

| 任务 | 位置 | 备注 |
|---|---|---|
| 共享初始化 | `src/main/java/com/euphony/better_client/BetterClient.java` | 模组公共入口 |
| 客户端功能事件 | `src/main/java/com/euphony/better_client/client` | HUD、tooltip、命令、renderer |
| 配置与界面 | `src/main/java/com/euphony/better_client/config` | `screen/category` 最密集 |
| Mixin / accessor | `src/main/java/com/euphony/better_client/mixin` | 注入点与字段桥接 |
| 平台抽象 | `src/main/java/com/euphony/better_client/platform` | 只走 `Platform` / `PlatformServices` |
| Access widener | `src/main/resources/better_client.accesswidener` | 访问私有成员的首选通道 |
| Mixin 清单 | `src/main/resources/better_client.mixins.json` | 先登记再新增 mixin |
| 生成资源 | `src/generated/resources` | 由 NeoForge datagen 产出 |

## CONVENTIONS

- 保持平台无关；不要直接 import Fabric API 或 NeoForge API。
- 需要平台差异时，扩展 `BetterClientPlatform`，再由 `PlatformServices` 分发。
- `client` / `config` / `mixin` / `utils` 是高密度目录，改动前先找同类实现对齐风格。
- 新增 mixin 时，同时更新 `better_client.mixins.json`；新增可访问私有成员时，优先考虑 widener / `@Shadow` / `@Accessor`。
- datagen 生成的资源归 `common/src/generated/resources`，不要手改生成物来代替逻辑修复。

## ANTI-PATTERNS

- 不要把加载器专属注册逻辑放进 `common`。
- 不要直接写内联全限定名；Minecraft / NeoForge / Fabric 类型都先 import。
- 不要为了拿私有字段去写反射；先看 access widener、accessor、`@Shadow`。
- 不要绕过 `Platform` 直接从共享代码触碰平台实现类。

## MIXIN RULES

- injected 方法名、`@Unique` 方法名、accessor 方法名统一加 `better_client$` 前缀。
- 受控 `(TargetType) (Object) this` 只允许出现在明确目标类型的 mixin 场景。
- 注入前先确认当前映射签名；不要为“兼容未知版本”保留反射后门。
- accessor 保持纯接口，不在里面写业务逻辑。

## COMMANDS

```powershell
# 继承根 AGENTS 里的 Java 25 / root build 规则
.\gradlew build
.\gradlew :common:build
```

## NOTES

- `config`、`client`、`mixin`、`utils` 文件数最高，但当前仍适合由这一层统一约束，不必继续下钻新增 AGENTS。
- `ChatHistoryManager` 等服务类会配合 accessor/mixin 工作，跨目录修改时先核对依赖链。
