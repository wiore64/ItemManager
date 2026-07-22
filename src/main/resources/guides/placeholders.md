# CustomItemManager | Placeholders

The plugin supports a set of placeholders that can be used inside certain messages in the `messages.yml` config file.

The available placeholders are:

* **`{USAGE}`** — Displays the correct command usage.
* **`{ITEM}`** — Displays the item name or identifier.
* **`{PLAYER}`** — Displays the player's name.
* **`{AMOUNT}`** — Displays the amount value.
* **`{ITEMS}`** — Displays a list of all registered items.
* **`{INDEX}`** — Displays a lore line number.
* **`{MAX_INDEX}`** — Displays the maximum available lore line number.
* **`{MODEL}`** — Displays the custom model identifier.
* **`{ENCHANTMENT}`** — Displays the enchantment identifier.
* **`{LEVEL}`** — Displays the enchantment level.
* **`{KEY}`** — Displays the item's key.

## Complete Placeholder Reference

| Placeholder     | Allowed Message Keys                                                                                                                                                                                           |
|-----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `{USAGE}`       | `shared-cmd.invalid-arguments`                                                                                                                                                                                 |
| `{ITEM}`        | `add-item-cmd.item-already-registered`, `remove-item-cmd.item-not-registered`, `give-item-cmd.item-not-found`, `add-item-cmd.item-registered`, `remove-item-cmd.item-unregistered`, `give-item-cmd.item-given` |
| `{PLAYER}`      | `give-item-cmd.player-not-found`, `give-item-cmd.item-given`                                                                                                                                                   |
| `{AMOUNT}`      | `give-item-cmd.invalid-item-amount`, `give-item-cmd.item-given`                                                                                                                                                |
| `{ITEMS}`       | `list-items-cmd.item-list`                                                                                                                                                                                     |
| `{INDEX}`       | `manage-item-cmd.lore-bad-index`, `manage-item-cmd.lore-large-index`                                                                                                                                           |
| `{MAX_INDEX}`   | `manage-item-cmd.lore-bad-index`, `manage-item-cmd.lore-large-index`                                                                                                                                           |
| `{MODEL}`       | `manage-item-cmd.invalid-model-id`                                                                                                                                                                             |
| `{ENCHANTMENT}` | `manage-item-cmd.unknown-enchantment`, `manage-item-cmd.enchantment-already-present`, `manage-item-cmd.enchantment-not-present`                                                                                |
| `{LEVEL}`       | `manage-item-cmd.invalid-enchantment-level`, `manage-item-cmd.out-of-bounds-enchantment-level`, `manage-item-cmd.enchantment-already-present`                                                                  |
| `{KEY}`         | `manage-item-cmd.invalid-key`, `manage-item-cmd.duplicate-key`, `manage-item-cmd.key-not-found`                                                                                                                |

Placeholders should only be used in the message keys listed above. Using a placeholder in any other message will result
in it being displayed as plain text.