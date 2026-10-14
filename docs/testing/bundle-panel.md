# Bundle panel validation

Issue [#18](https://github.com/pynickle/Better-Client/issues/18) specifies the transfer rules. The presentation and update rules below incorporate the follow-up design confirmed on 2026-10-14 and supersede that issue's in-panel view/deposit buttons and pointer-exit compaction. The panel uses the common `BundlePanelSession` boundary and a Minecraft menu adapter, with shared survival-screen mixins for both loaders. Bundle up retains its existing implementation and policy.

The presentation follows the player's inventory: the frame and 18-pixel item cells use the vanilla inventory texture, controls use vanilla button sprites and EditBox, and quantities use vanilla item decorations. The title, search field, grid, and footer share one side padding, so their left and right edges line up; the grid's columns absorb the remainder rather than centering themselves. With the recipe book closed, the inventory and its left sidebar are centered together so a normal window does not unnecessarily fall back to a toggle. The narrow fallback replaces only the inventory rectangle, and its bundle button is attached inside the inventory header.

## Confirmed presentation and update rules

- Both views remain available in settings, with Merged view as the default. The panel has a search field and its expand/collapse control, without view-switch or deposit buttons.
- Group headings show a bundle icon, its styled name, and percentage used. Capacity is dark gray below 75%, dark orange from 75%, and red when full. Headings are informational; click a group's item cells or blank cells to deposit. Empty bags retain one row of cells.
- Items and group headings use the original item tooltip, including tooltip images and styles. No operation guide or failure reason is appended. Failures appear for four seconds on the footer line.
- Quantity labels retain exact totals and shrink only when the current font's measured width, including the shadow, exceeds the cell's available space. Every label pins the same bottom-right corner as the vanilla count, so shrink never moves it; one- and two-digit totals match the survival inventory exactly. Item size, durability bars, and cooldown overlays retain their normal rendering.
- The vanilla EditBox is registered as an inventory-screen widget and receives screen focus, including the original IME preedit route. Search focus is released when the panel hides or closes.
- Counts, new items, and bag movement update immediately. While any keyboard or mouse button is held, or a transfer is pending, existing cells retain their positions and exhausted cells look empty. Newly discovered entries are appended; a growing group may temporarily continue at the end to avoid moving later groups.
- Once all keys/buttons are released and transfers finish, empty positions are removed without re-sorting surviving entries. Pointer position and a carried cursor stack do not prevent compaction. Opening a new inventory or changing search establishes the original sort again.
- Transfers serialize repeated click and wheel intents. Only changes to the current menu identity, cursor, or involved slots stop a pending sequence; unrelated slots and hover-selection changes do not freeze the panel. Collapsing or closing the panel cancels pending transfers and recovers owned temporary items when safe.

## Automated checks

Use JDK 25:

```powershell
$env:JAVA_HOME = "D:\Program Files\Eclipse Adoptium\graalvm-jdk-25.0.3+9.1"
.\gradlew :common:test :common:compileJava :fabric:compileJava :neoforge:compileJava
```

`BundlePanelSessionTest` supplies inventory snapshots and gestures through a stateful menu adapter. It covers complete first-level contents, bag regions, stacking identity, views/search, stable cells with live additions/removals, release-driven compaction without re-sorting, extraction quantities and stack limits, occupied cursors, Main inventory transfers, deterministic deposit priority, explicit nesting direction, temporary space, repeated click/wheel actions, unrelated changes, interruption/conservation, and layout placement. The screen consumes the tested session cell projection directly. The adapter models ordinary bundle and slot clicks, including whole-entry retrieval and remainder insertion that changes selection indices. It does not prove acceptance by a real server.

Mouse event buttons and container-click buttons have different values in Minecraft 26.3. Screen controls use `InputConstants.MOUSE_BUTTON_LEFT` and `MOUSE_BUTTON_RIGHT`; the menu adapter uses container primary/secondary values 0 and 1. Check real left and right clicks when validating controls.

Before retrieval, the menu adapter clears bundle selection on both client and server and then selects the current full-content index. This is intentional: selection is a toggle, and its index is not serialized with the contents. A locally matching index alone cannot establish server selection after reconciliation.

## Live checks still required

Fabric and NeoForge development clients were launched to check startup and resource loading on 2026-10-14. Neither development instance has DebugBridge installed, and the inspected published bridge releases do not provide a Minecraft 26.3 build. No in-world/server transfer, visual-layout, translation, or mouse-mod compatibility result is claimed from those startup checks.

Before treating the feature as validated, test both loaders against an unmodified Minecraft 26.3 server:

- Retrieve hidden entries; test one, rounded-up half, one stack, and all matching items with limits 64, 16, and 1. Merge a stack across bags and confirm Grouped view confinement.
- Test empty, compatible, incompatible, and full cursors. Deposit one and an entire carried stack; verify partial-capacity leftovers. Exercise full Main inventories and partially filled destinations without an empty temporary slot.
- Explicitly nest a held bundle into a chosen target, then retrieve it. Confirm automatic and wheel deposits skip bundles and Bundle up remains unchanged.
- Repeat modified-wheel transfers, introduce delay and server corrections, close the menu midway, and reopen it to confirm authoritative counts/components. Verify transient items remain owned and no item is dropped.
- Check multiple window sizes and GUI scales, recipe-book visibility, status effects, narrow-screen switching and input shielding. Test settings-only view selection, collapse preferences, search, scroll, empty/no-bag states, and English/Simplified Chinese text.
- Enter Chinese through an IME, including unfinished pinyin, candidate selection, focus changes, resize, and closing/reopening the panel. Verify item and bundle tooltips retain their native text, content preview and custom style, with no appended guide.
- Compare neighboring counts such as 5 and 128, plus 99/100 and 999/1000, at small and large GUI scales and with a resource-pack font. The full quantities and their shadows must stay separated inside their cells; check that durability bars and cooldown overlays still render.
- Hold Shift/Ctrl or a mouse button while transferring: counts and new entries must update, empty cells stay empty, and other cells keep their positions. Release all inputs with the pointer still over the grid and a stack on the cursor: holes should close without re-sorting. Move a bag away and back; its heading and contents must refresh. Check capacity text at 74%, 75%, and 100%, and footer failures disappearing after four seconds.
- Repeat input smoke tests with Mouse Tweaks or Item Scroller to detect duplicate transfers. Normal interactions outside the panel must continue to use existing inventory controls.

The controller stops a transaction when the menu identity, cursor or involved slot contents differ from its prediction. Unrelated changes remain visible without aborting that transaction. It recovers owned temporary items when the same valid menu permits safe clicks; interruption does not promise atomic rollback. No DebugBridge was reachable during this follow-up, so the IME, tooltip appearance, and live server checks remain manual validation items.
