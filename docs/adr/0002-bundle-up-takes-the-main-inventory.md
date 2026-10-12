# Bundle up takes the main inventory

Bundle up fills the bundle under the pointer. The items come from the main inventory only: the player's storage slots 9 through 35. The open container, the hotbar, and the equipment slots stay where they are. The bundle itself may sit in any of those places.

The action frees storage space. Taking from the container the player has open would loot it, and taking the hotbar or equipment would strip what the player is using.

## Considered Options

- Also take the open container. That is what the chest screen used to do, and it fights the storage goal.
- Also take the hotbar. Those slots are the active loadout.
- Also take the equipment slots. Armor would be pulled into the bundle, because a bundle click accepts the equipped item before the armor slot can reject the bundle.
- Main inventory only. The same rule applies on every screen that shows those slots.

## Consequences

- A creative inventory tab uses the same storage slots. Tabs that do not show them do nothing.
- Another bundle in the main inventory can go into the target bundle, under the vanilla bundle rule.
- The Bundle Up key stays unbound. The setting text tells the player to bind it, and it does not name a default key.
- There is no separate switch for the hotbar, the equipment slots, or the open container.
