# Better Client

Client-side Minecraft mod. These terms name concepts the features share.

## Chat history

**History boundary**:
The point where the current restore separates newly received chat from the chat loaded for that restore. It records when that session disconnected, and it sits above the anchor message.
_Avoid_: Separator, separator line, restored-history separator, marker

**Anchor message**:
The newest real message still in the restored span.

**Restored span**:
The real chat messages loaded by the current restore.
_Avoid_: Restored history, old chat

## Bundle up

**Bundle up**:
Filling the bundle under the pointer with items from the main inventory.
_Avoid_: Sort, deposit, loot, quick-store

**Main inventory**:
The player's storage slots, excluding the hotbar and the equipment slots.
_Avoid_: Inventory, backpack

**Equipment slots**:
The armor slots and the offhand slot.
_Avoid_: Armor slots

## Trading HUD

**Trading HUD anchor**:
The screen corner the trading HUD is pinned to.
_Avoid_: Alignment, right alignment, anchor

**Enchantment caption**:
The enchantment names of one offer's result, shown with that offer.
_Avoid_: Enchantment text, label
