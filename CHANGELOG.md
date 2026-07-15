# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/2.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.0] - 2026-07-xx

### Added

- Now the plugin will perform even faster!

### Changed

- Cleaned up some code under the hood.

### Removed

- No more general config since log level option is decided to be removed.

## [1.1.3] - 2026-07-11

### Changed

- Basic example in items guides now contain the correct color.
- Improved update checking mechanism.
- Mostly updates under the hood of the plugin.

## [1.1.2] - 2026-07-08

### Added

- Now the manager has a prefix it uses in its messages.
- Now the message config allows you to set a custom prefix.
- Guide files are stored locally now too.

### Changed

- From now on, ItemManager is to be named CustomItemManager to provide more clarity on the plugin's functionality.
- Updated the structure of the message config.
- Updated the structure of the general config.

## [1.1.1] - 2026-07-06

### Added

- Added a first basic test.
- Now all fields are optional except material.
- Now if an attribute has no modifiers, the plugin will warn about it.
- Added internal ids to track items regardless of their set item ids by admins.

### Changed

- Links to the guide files were updated.
- Updated the project's artifact ID.
- Updated the update check link.

## [1.1.0] - 2026-07-04

This is a minor update for the plugin, mostly updating the underlying structure.

### Added

- Added a guide for item configuration, as well as to 'items.yml' itself.
- Added a field for custom model data. It is set as optional to not break configs. It will be added automatically
  to all new items. You may use it for resource packs!

### Changed

- Updated serialization/deserialization system to address future maintainability.
- Update link leads to the plugin's webpage on PaperMC now.
- Now the plugin supports storage of namespaced keys not only for default enchantments and attributes. However, those
  are still used with the default registry access.
- Attempted to improve display name parsing. Whenever an item is added via a command, its translatable key is stored,
  so that whenever you view with a different language, it shows it differently. Only for default names.

## [1.0.1] - 2026-07-03

This is a patch for the plugin, addressing a few issues.

### Fixed

- The update checker used to log the current and latest versions, which was an unintended behavior. From now on, it
  does not do it anymore, only notifying you whether there is an update or not.
- When no items remained in the items.yml after executing /itemmanager remove, the YAML parser tended to write {} to
  the file, which was undesirable. Thus, that was fixed, and that does not happen anymore.

### Changed

- Instead of using an error-prone way of checking for the material specified in the items.yml config, now a safer and
  generally better approach is used, allowing for a more reliable checking of the specified item material.

## [1.0.0] - 2026-07-02

This is the first release version of the plugin.

### Added

- Command management system.
- Configuration management system.
- Custom item storage management system.
- Update checking utility.
- API for a future use by other plugins.
- PLACEHOLDERS.md to help with configuring the message config.
