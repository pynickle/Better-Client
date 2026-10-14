# Bundle panel interaction references

Research date: 2026-10-13. This is a comparison of existing interactions and a set of proposals for the design interview, not an accepted feature specification. The terminology follows the root `GLOSSARY.md`. No mod implementation or runtime compatibility test was performed for this note.

## Sources and versions

| Reference | Scope checked |
| --- | --- |
| Applied Energistics 2 | Author-maintained terminal guide and `MEStorageMenu` on `main`, observed at commit `b141e91764897d3844c9debd4210396b5f9b5238`. Its [Gradle properties](https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/b141e91764897d3844c9debd4210396b5f9b5238/gradle.properties) target Minecraft 26.3. This is a source snapshot, not a claim about every AE2 release. |
| Refined Storage | Official [Grid documentation](https://refinedmods.com/refined-storage/viewing-resources/grid.html), the site's **Latest / snapshot** documentation at retrieval. The selector lists v3.2.1, but the cited unversioned page must not be treated as a frozen v3.2.1 manual. |
| Mouse Tweaks | Author's [project description](https://www.curseforge.com/minecraft/mc-mods/mouse-tweaks) and `master` source, observed at commit `e8ecd08ed94192f6f19d3cbb630707c8670a4d66`. [Gradle properties](https://github.com/YaLTeR/MouseTweaks/blob/e8ecd08ed94192f6f19d3cbb630707c8670a4d66/gradle.properties) identify version 2.31 / Minecraft 26.3. |
| Item Scroller | Author-maintained [project description](https://www.curseforge.com/minecraft/mc-mods/item-scroller). The page currently offers 0.33.1 / Minecraft 26.3 and explicitly describes 1.20.5+ as Sakura-Ryoko-maintained ports. Its description spans older releases, so it documents intended gestures rather than proving every 0.33.1 implementation detail. |

## What the references actually do

### Storage grids

AE2's [terminal guide](https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/b141e91764897d3844c9debd4210396b5f9b5238/guidebook/items-blocks-machines/terminals.md) describes left-click taking a stack and right-click taking half a stack. Holding Shift freezes displayed positions. The pick-block binding, normally middle-click, opens the autocrafting amount screen for craftable resources; it is not documented there as a general take-one shortcut.

The [AE2 server-side interaction handler](https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/b141e91764897d3844c9debd4210396b5f9b5238/src/main/java/appeng/menu/me/common/MEStorageMenu.java) gives the following precise item semantics:

| Action | Behavior |
| --- | --- |
| `PICKUP_OR_SET_DOWN` | Empty cursor: take a stack. Occupied cursor: deposit carried stack. |
| `SPLIT_OR_PLACE_SINGLE` | Empty cursor: take rounded-up half. Occupied cursor: deposit one. |
| `PICKUP_SINGLE` / `ROLL_UP` | Take one; require empty cursor or matching stack with room. |
| `ROLL_DOWN` | Deposit one carried item, regardless of hovered type. |
| `SHIFT_CLICK` | Fill the first suitable player-inventory destination slot. |
| `MOVE_REGION` | Repeat same-item transfer with a bounded loop until unsuccessful. |
| `CREATIVE_DUPLICATE` | Create a stack only with creative `instabuild`. |

The half request is `ceil(min(accessible count, maximum stack size) / 2)`, before power/storage constraints: 100 stones requests 32; 11 requests 6. Front-end bindings for single pickup, bulk transfer, and rolling were not verified. Action names alone do not establish Ctrl/middle-click or physical wheel direction. [Source](https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/b141e91764897d3844c9debd4210396b5f9b5238/src/main/java/appeng/menu/me/common/MEStorageMenu.java)

Refined Storage's [Grid controls](https://refinedmods.com/refined-storage/viewing-resources/grid.html#controls) describe a different complete gesture mapping:

| Gesture | Documented behavior |
| --- | --- |
| Empty cursor, left / right click | Take a maximum stack / half the maximum stack size. |
| Occupied cursor, left / right click | Insert the held resources / one held resource. |
| Shift + wheel down / up over the Grid | Extract one / insert one. |
| Ctrl or Cmd + wheel down over the Grid | Extract a resource onto the cursor. |
| Shift + click a player-inventory resource | Insert that resource into storage. |
| Ctrl or Cmd + click with empty cursor | Open the autocrafting request, even when stock exists. |

The page also says sorting is deferred while Shift is held. It does not precisely specify the low-stock rounding for half stacks or explicitly document Shift-click extraction from the Grid. Those details remain unverified here; AE2's half formula must not be attributed to Refined Storage without checking its implementation. [Source](https://refinedmods.com/refined-storage/viewing-resources/grid.html)

### Mouse utilities

Mouse Tweaks' [author description](https://www.curseforge.com/minecraft/mc-mods/mouse-tweaks) documents these defaults: wheel down sends individual items out of the hovered stack; wheel up draws matching items into it from another inventory. Direction can be reversed or made sensitive to inventory position. Right-drag deposits one item each time a slot is crossed, including repeated crossings. Left-drag after picking up an item gathers matching stacks; adding Shift transfers matching crossed stacks to the other inventory. Starting empty with Shift + left-drag transfers crossed stacks without a type filter.

[Mouse Tweaks 2.31 `Main.java`](https://github.com/YaLTeR/MouseTweaks/blob/e8ecd08ed94192f6f19d3cbb630707c8670a4d66/src/main/java/yalter/mousetweaks/Main.java) clarifies limitations that the short description omits:

- Drag processing returns immediately while the pointer stays in the same slot. This is repeated **slot entry**, not automatic repeated transfer while holding still.
- Ordinary wheel transfer does nothing when the cursor holds a compatible stack; an incompatible held stack may be temporarily swapped and restored where slot rules allow.
- Unmodified left-drag gathering skips a crossed stack when combining its entire count would overflow the cursor stack.
- Wheel input is accumulated into an item count; valid-slot scrolling remains consumed even when no item can move. This avoids suddenly handing the same gesture to another handler after a slot runs out.

Item Scroller's [author description](https://www.curseforge.com/minecraft/mc-mods/item-scroller) documents a broader set of gestures:

| Gesture | Documented behavior |
| --- | --- |
| Wheel | Move one item to or from the hovered slot. |
| Shift + wheel | Move whole matching stacks; the hovered stack moves last. |
| Ctrl + wheel, or Alt + click | Move all matching stacks to or from the other inventory. |
| Ctrl + Shift + wheel, or Alt + Shift + click | Move everything to or from the other inventory. |
| Shift + left-drag | Move crossed stacks. |
| Shift + right-drag | Move all but one item from each crossed stack. |
| Ctrl + left/right-drag | Move one item from each crossed stack. |
| Occupied cursor, Shift + click an empty slot | Move all matching items to that inventory. |

The description exposes configurable scroll direction/interpretation; it does not establish one universal direction for every configuration. Its held-button actions explicitly require dragging across slots. Stationary hold-to-repeat for ordinary storage extraction, and general occupied-cursor wheel behavior, were not verified.

## Implications for the bundle panel interview

The following are **proposals**, not decisions accepted by the user:

1. Offer maximum-stack, half-stack, and one-item extraction. A familiar storage-style candidate is left-click for a stack, right-click for half, and a dedicated modified click for one. A right-click-for-one candidate is also reasonable, but would be a deliberate departure from AE2/Refined Storage's documented empty-cursor right-click behavior.
2. Preserve the user's already accepted Shift + left-click destination of the **main inventory**. Do not silently inherit another mod's broader player-inventory destination or use of the hotbar.
3. Leave unmodified wheel input for the scrollable item list. Require a modifier for wheel transfer; Refined Storage demonstrates that split. Choose and show the direction explicitly. Mouse Tweaks' slot-relative direction, Refined Storage's Grid direction, and AE2's server action names are not interchangeable evidence.
4. If normal clicks insert carried items as in storage grids, a one-item extraction shortcut should remain an extraction operation when holding a matching, non-full stack. Reject incompatible/full-cursor extraction. Otherwise repeated take-one clicks could alternate between extraction and deposit. The alternative is a consistently extraction-only item list with insertion restricted to its explicit deposit control; that trade-off needs a user decision.
5. Give all-matching extraction a distinct gesture from the already agreed one-stack Shift-click. It should stop at the accepted destination's capacity. Grouped view should restrict its source to that bundle; merged view can draw from its included bundles. This is a proposed translation of bulk-transfer behavior, not a claim that a reference mod implements bundles this way.
6. Treat stationary hold-to-repeat as a separate product decision. Neither the Mouse Tweaks code checked here nor the Item Scroller description establishes it as the ordinary storage gesture. If added, bind a held gesture to the original item identity and stop on release, screen changes, or exhausted capacity. These stop rules are recommendations.
7. Keep the target entry visually stable during repeated interactions. AE2 and Refined Storage both provide Shift-related sorting freezes; a bundle list must also avoid moving a different item under an active pointer when stock disappears. Exact freezing/tombstone behavior remains a design choice.

## Vanilla bundle constraint reported by the parallel source investigation

The accompanying Minecraft 26.3 investigation used mcdev static sources for `BundleItem.overrideOtherStackedOnMe`: selecting and extracting a bundle entry obtains that entire stored stack; right-clicking the bundle is not a one-item insertion primitive. Fine quantities therefore require a sequence of vanilla slot interactions, rather than merely substituting a right-click on the bundle.

Two conservative sequences identified there are:

- **One item to cursor:** use an ordinary empty temporary slot T; extract the stored stack, right-click T to place one, left-click the bundle to return the remainder, then left-click T to retrieve the one item.
- **One item to main inventory:** extract the stored stack, right-click a destination slot that can accept one, then left-click the bundle to return the remainder. That destination can be a partially filled matching stack; an additional empty slot is not inherently required for this sequence.

Return-to-bundle clicks must be skipped when the cursor is empty, or they would pick up the bundle itself. Returning a remainder can also change the bundle's internal item order (`BundleContents.Mutable.tryInsert`), so subsequent operations must resolve the current item index again.

These are candidate implementation strategies supplied by that source investigation, not proof that all possible algorithms require an empty temporary slot. Their cursor occupancy, interruptions, and server synchronization still require implementation-level validation. This research note does not claim an in-game or multiplayer test of them.
