# ItemManager

A powerful and centralized custom item management plugin for Minecraft servers running Paper/Spigot 1.21+.

ItemManager makes it easy to define, register, and distribute custom items while keeping their configuration in one place. Instead of every plugin implementing its own custom item system, ItemManager provides a shared registry that can also be used by your own plugins through custom item keys.

## Features

* Register custom items through configuration.
* Customize:

    * Material
    * Display name
    * Lore
    * Enchantments
    * Item attributes
    * Custom item keys
* Give registered items through commands.
* Reload item definitions without restarting the server.
* List all registered items.
* Centralized item registry for easier maintenance.
* Designed to integrate with your own plugins using persistent custom keys.

## Why ItemManager?

Many plugins implement their own custom items independently, making maintenance and interoperability difficult.

ItemManager solves this by acting as a central registry for all custom items on your server. Your plugins can simply reference an item's unique key instead of recreating its metadata, allowing multiple plugins to recognize and work with the same item.

For example:

* A quest plugin can require a specific custom item.
* A crafting plugin can consume that same item.
* A skills plugin can detect it and grant bonuses.
* An events plugin can reward it.

All without each plugin having to define the item separately.

## Commands

| Command               | Description                         |
| --------------------- | ----------------------------------- |
| `/itemmanager add`    | Register a new custom item.         |
| `/itemmanager remove` | Remove a registered item.           |
| `/itemmanager give`   | Give a registered item to a player. |
| `/itemmanager list`   | List all registered items.          |
| `/itemmanager reload` | Reload the plugin configuration.    |
| `/itemmanager help`   | Display command help.               |

### Aliases

* `/im`
* `/imanager`
* `/itemm`

## Permissions

| Permission                   | Description                                             |
| ---------------------------- | ------------------------------------------------------- |
| `itemmanager.*`              | Grants access to all ItemManager commands and features. |
| `itemmanager.command`        | Allows use of `/itemmanager`.                           |
| `itemmanager.command.add`    | Allows adding items.                                    |
| `itemmanager.command.remove` | Allows removing items.                                  |
| `itemmanager.command.give`   | Allows giving custom items.                             |
| `itemmanager.command.list`   | Allows listing registered items.                        |
| `itemmanager.command.reload` | Allows reloading the configuration.                     |
| `itemmanager.command.help`   | Allows viewing help.                                    |

By default, all permissions are granted to server operators.

## Plugin Integration

ItemManager is intended to serve as a shared item registry.

Each registered item may contain one or more custom keys that other plugins can use to identify it. This allows your own plugins to integrate seamlessly without depending on item names, lore, or enchantments, which may change over time.

Instead, plugins can simply check for a specific key and respond accordingly.

## Compatibility

* Minecraft 1.21+
* Paper
* Spigot (where supported)

## Installing

1. Go to the latest release.
2. Click to download the JAR.
3. Place it in your server's folder.
4. Done! When a new update appears, a notification will be shown.
