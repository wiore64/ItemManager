# CustomItemManager | Placeholders

The plugin supports a set of placeholders that can be used inside certain messages in the `messages.yml` configuration file.

The available placeholders are:

* **`{USAGE}`** — Displays the correct command usage.
* **`{ITEM}`** — Displays the item name or identifier.
* **`{PLAYER}`** — Displays the player's name.
* **`{AMOUNT}`** — Displays the amount value.
* **`{ITEMS}`** — Displays a list of all registered items.

## Placeholder Usage by Message Key

### `{USAGE}`

Used in:

```yaml
invalid-arguments
```

Example:

```yaml
invalid-arguments: "<red>Invalid arguments.</red> Usage: <gold>{USAGE}</gold>"
```

Displays the command usage when invalid arguments are provided.

---

### `{ITEM}`

Used in:

```yaml
item-already-registered
item-not-found
item-registered
item-unregistered
item-given
```

Example:

```yaml
item-registered: "<green>Item {ITEM} registered.</green>"
```

Displays the affected item's name or identifier.

---

### `{PLAYER}`

Used in:

```yaml
player-not-found
item-given
```

Example:

```yaml
item-given: "<green>Gave {AMOUNT}x {ITEM} to {PLAYER}.</green>"
```

Displays the player's name involved in the action.

---

### `{AMOUNT}`

Used in:

```yaml
invalid-amount
item-given
```

Example:

```yaml
invalid-amount: "<gold>Invalid amount {AMOUNT}. Must be a positive integer.</gold>"
```

Displays the provided amount value.

---

### `{ITEMS}`

Used in:

```yaml
item-list
```

Example:

```yaml
item-list: "Registered items: <gold>{ITEMS}</gold>"
```

Displays all currently registered items.

---

## Complete Placeholder Reference

| Placeholder | Allowed Message Keys                                                                              |
| ----------- | ------------------------------------------------------------------------------------------------- |
| `{USAGE}`   | `invalid-arguments`                                                                               |
| `{ITEM}`    | `item-already-registered`, `item-not-found`, `item-registered`, `item-unregistered`, `item-given` |
| `{PLAYER}`  | `player-not-found`, `item-given`                                                                  |
| `{AMOUNT}`  | `invalid-amount`, `item-given`                                                                    |
| `{ITEMS}`   | `item-list`                                                                                       |

Placeholders should only be used in the message keys listed above. Using a placeholder in another message will result in it being displayed as plain text.
