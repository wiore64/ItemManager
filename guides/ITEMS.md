# Configuring a Custom Item

This guide explains how a **custom item** is defined in ``items.yml``. Most of the fields are **mandatory**, but **may be left empty**, so _you only need to set what you actually want to customize_.

If you're ever **unsure** about a value (_such as a material name or an attribute key_), check _**one of the related links below**_. They point to the **official Paper documentation** for your server version.

Replace ```{VERSION}``` with your server version (_e.g. 1.21, 26.2, etc._).

- Material names:
  <https://jd.papermc.io/paper/{VERSION}/org/bukkit/Material.html>
- Enchantment keys:
  <https://jd.papermc.io/paper/{VERSION}/io/papermc/paper/registry/keys/EnchantmentKeys.html>
- Attribute keys:
  <https://jd.papermc.io/paper/{VERSION}/io/papermc/paper/registry/keys/AttributeKeys.html>
- Attribute modifier operations:
  <https://jd.papermc.io/paper/{VERSION}/org/bukkit/attribute/AttributeModifier.Operation.html>
- Equipment slots:
  <https://jd.papermc.io/paper/{VERSION}/org/bukkit/inventory/EquipmentSlotGroup.html>

## A Basic Example

```yaml
example_item:
  material: DIRT
  display-name: <brown>Mythic Dirt</brown>
  lore: [ "A crazy story behind it..." ]
  enchantments: []
  attributes: []
  keys: []
```

## An Advanced Example

```yaml
example_item:
  material: COPPER_SWORD
  display-name: <#F55E27><bold>Copper sword</bold></#F55E27>
  model-id: 1001
  lore:
    - <gray>Forged by dwarves.
    - <yellow>Handle with care.
  enchantments:
    - name: sharpness
      level: 10
    - name: unbreaking
      level: 3
  attributes:
    - name: attack_speed
      modifiers:
        - operation: ADD_NUMBER
          amount: 5
          slot: mainhand
    - name: attack_damage
      modifiers:
        - operation: ADD_SCALAR
          amount: 0.5
          slot: mainhand
  keys:
    - plugin:key
    - key
```

## Fields

### `material`

The base Minecraft item. Must be specified.

Example:

```yaml
material: COPPER_SWORD
```

Use the material names from the Paper API documentation for your server version.

### `name`

The item's display name.

```yaml
display-name: <#F55E27><bold>Copper sword</bold></#F55E27>
```

Supports MiniMessage formatting. As of now, when item with default name added via a command, ItemManager uses its 
translatable key, making it vary depending on the set language of your client. Also, if you try to add the same
item multiple times, its color codes may stack up. Not the ones set by you manually, however.

---

### `lore`

The item's lore.

```yaml
lore:
  - <gray>Forged by dwarves.
  - <yellow>Handle with care.
```

Supports MiniMessage formatting. Default is italic, light purple style.

---

### `model-id`

The item's model ID.

```yaml
model-id: -1
```

Used for resource packs to define the item's custom model data. Optional field. -1 means none.

---

### `enchantments`

Adds enchantments to the item.

```yaml
enchantments:
  - name: minecraft:sharpness
    level: 10
  - name: unbreaking
    level: 3
```

Allows unsafe enchantment combinations and unsafe enchantment levels (0 to 255).

---

### `attributes`

Adds attribute modifiers.

```yaml
attributes:
  - name: attack_speed
    modifiers:
      - operation: ADD_NUMBER
        amount: 5
        slot: mainhand
  - name: minecraft:attack_damage
    modifiers:
      - operation: ADD_SCALAR
        amount: 0.5
        slot: mainhand
```

Refer to the Paper documentation for the available attribute keys, operations, and equipment slots.
You may also stack multiple modifiers on multiple attributes for different equipment slots. If slot
is not specified, ANY is used by default.

## Keys

Adds namespaced keys.

```yaml
keys:
  - plugin:key
  - key
```

If plugin is not clarified, ItemManager is used as the namespace instead.

## End

**You've reached the end of the current documentation!**

**_Hope that helped you to understand the process of configuration your own custom items more!_**