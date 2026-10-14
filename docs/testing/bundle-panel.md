# Bundle panel validation

Issue [#18](https://github.com/pynickle/Better-Client/issues/18) is the authoritative specification. The panel uses the common `BundlePanelSession` boundary and a Minecraft menu adapter, with shared survival-screen mixins for both loaders. Bundle up retains its existing implementation and policy.

The presentation follows the player's inventory: the frame and 18-pixel item cells use the vanilla inventory texture, controls use vanilla button sprites and EditBox, and quantities use vanilla item decorations. With the recipe book closed, the inventory and its left sidebar are centered together so a normal window does not unnecessarily fall back to a toggle. The narrow fallback replaces only the inventory rectangle, and its bundle button is attached inside the inventory header.

## Automated checks

Use JDK 25:

```powershell
$env:JAVA_HOME = "D:\Program Files\Eclipse Adoptium\graalvm-jdk-25.0.3+9.1"
.\gradlew :common:test :common:compileJava :fabric:compileJava :neoforge:compileJava
```

`BundlePanelSessionTest` supplies inventory snapshots and gestures through a stateful menu adapter. It covers complete first-level contents, bag regions, stacking identity, views/search/stable depleted entries, extraction quantities and stack limits, occupied cursors, Main inventory transfers, deterministic deposit priority, explicit nesting direction, temporary space, repeated wheel actions, interruption/conservation, and layout placement. The adapter models ordinary bundle and slot clicks, including whole-entry retrieval and remainder insertion that changes selection indices. It does not prove acceptance by a real server.

Mouse event buttons and container-click buttons have different values in Minecraft 26.3. Screen controls use `InputConstants.MOUSE_BUTTON_LEFT` and `MOUSE_BUTTON_RIGHT`; the menu adapter uses container primary/secondary values 0 and 1. Check real left and right clicks when validating controls.

Before retrieval, the menu adapter clears bundle selection on both client and server and then selects the current full-content index. This is intentional: selection is a toggle, and its index is not serialized with the contents. A locally matching index alone cannot establish server selection after reconciliation.

## Live checks still required

Fabric and NeoForge development clients were launched to check startup and resource loading on 2026-10-14. Neither development instance has DebugBridge installed, and the inspected published bridge releases do not provide a Minecraft 26.3 build. No in-world/server transfer, visual-layout, translation, or mouse-mod compatibility result is claimed from those startup checks.

Before treating the feature as validated, test both loaders against an unmodified Minecraft 26.3 server:

- Retrieve hidden entries; test one, rounded-up half, one stack, and all matching items with limits 64, 16, and 1. Merge a stack across bags and confirm Grouped view confinement.
- Test empty, compatible, incompatible, and full cursors. Deposit one and an entire carried stack; verify partial-capacity leftovers. Exercise full Main inventories and partially filled destinations without an empty temporary slot.
- Explicitly nest a held bundle into a chosen target, then retrieve it. Confirm automatic and wheel deposits skip bundles and Bundle up remains unchanged.
- Repeat modified-wheel transfers, introduce delay and server corrections, close the menu midway, and reopen it to confirm authoritative counts/components. Verify transient items remain owned and no item is dropped.
- Check multiple window sizes and GUI scales, recipe-book visibility, status effects, narrow-screen switching and input shielding. Test all controls, search, scroll, empty/no-bag states, remembered view/collapse preferences, and English/Simplified Chinese text.
- Repeat input smoke tests with Mouse Tweaks or Item Scroller to detect duplicate transfers. Normal interactions outside the panel must continue to use existing inventory controls.

The controller stops a transaction when its predicted snapshot differs from the current menu. It recovers owned temporary items when the same valid menu permits safe clicks; interruption does not promise atomic rollback.
