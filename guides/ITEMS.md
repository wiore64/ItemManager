# CustomItemManager | Custom Items Config (2026-07-08)

This guide explains how a **custom item** is defined in ``items.yml``. The only two **required fields** are **material and internal ID**, where the _latter one is handled automatically_.

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

### `internal-id`

An internal ID managed by the plugin.

```yaml
internal-id: 123e4567-e89b-12d3-a456-426614174000
```

This is not to be modified. It is injected by the manager automatically. Its pure purpose is to allow tracking of the 
items issued by the plugin later on. Each custom item is issued such an ID upon creation, and exists only for internal use.
Modifying it will lead to a generation of a new ID, and thus, all previous items related to it will not be linked anymore.

### `material`

The base Minecraft item. Must be specified.

Example:

```yaml
material: COPPER_SWORD
```

Use the material names from the Paper API documentation for your server version.

### `name`

The item's display name. Optional.

```yaml
display-name: <#F55E27><bold>Copper sword</bold></#F55E27>
```

Supports MiniMessage formatting. As of now, when item with default name added via a command, CustomItemManager uses the 
item's translatable key, making it vary depending on the set language of your client. Though, it does not create a key
for display name, since it is default.

---

### `lore`

The item's lore. Optional.

```yaml
lore:
  - <gray>Forged by dwarves.
  - <yellow>Handle with care.
```

Supports MiniMessage formatting.

---

### `model-id`

The item's model ID. Optional.

```yaml
model-id: 1001
```

Used for resource packs to define the item's custom model data.

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

Allows unsafe enchantment combinations and unsafe enchantment levels (0 to 255). Illegal levels are clamped.

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

If namespace is not clarified, CustomItemManager is used as the namespace instead.

## End

**You've reached the end of the current documentation!**

**_Hope that helped you to understand the process of configuration your own custom items more!_**