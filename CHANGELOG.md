# CHANGELOG
## 1.4.2 - 1.20.1
**Requires Tinkers’ Construct 3.9.2.37+**

### Fixed
- Game crash related to changes in Tinkers' Construct 3.9.2.34

## 1.4.1 - 1.20.1
**Requires Tinkers’ Construct 3.9.1.19+**

Updated mod to 1.20.1

## 1.4.1
### Added
- Modifier texture for [Tinkers Reforged](https://www.curseforge.com/minecraft/mc-mods/tinkers-reforged) Longsword.
### Fixed
- Client crash when displaying [Tinkers Reforged](https://www.curseforge.com/minecraft/mc-mods/tinkers-reforged) Greatsword with Improvable modifier.

## 1.4.0
**Requires Tinkers' Construct 3.8.3.39+**

Updated mod to 1.19.2
### Changed
- Disabled XP for "extra attacks" as they allowed certain tools to be upgraded too quickly.
### Added
- Separate config settings for staff.
- Book entries for Improvable modifier (Encyclopedia and Mighty Smelting).
- Support for flying (Wings chestplate modifier and Slimelytra). XP is awarded for continuous flight (default 1 XP / 15 seconds). Added corresponding settings in server config.
- Support for sweeping and circle weapon attacks.
- Support for boots walker modifiers (plowing, path maker, snowdrift, flamewake, frost walker). Added corresponding settings in server config.
- Support for interaction modifiers (warping, flinging, springing, bonking, glowing, firestarter). Added corresponding settings in server config.
- New level up sound - Tinkers' 'Bonk' sound.
- Modifier texture for staffs.
### Fixed
- Config comments translations.
- Attacking armor stand gives XP.

## 1.3.0
**Requires Tinkers' Construct 3.6.3.113+**
### Changed
- Tools can now gain defence slot. This option is disabled by default. It can be enabled in server config (\*SlotTypeOrder/\*SlotTypeRandomPool).
### Added
- Support for shields and corresponding config settings.
- Modifier textures for [Materialis](https://www.curseforge.com/minecraft/mc-mods/materialis) and [Tinkers Reforged](https://www.curseforge.com/minecraft/mc-mods/tinkers-reforged) tools.
- Ukrainian translation (Thanks @ItzNyuel).
### Fixed
- Armor gets experience, when all damage is blocked by shield.
- Ranged weapons uses tool's slot configs.

## 1.2.0
**Requires Tinkers' Construct 3.6.2.92+**
### Changed
- **Below config changes will modify/reset some of your settings! Before updating the mod, backup your server config file to make it easier to restore your previous settings values.**
- Unified some config option names to be consistent throughout the mod.
- Grouped together levelling enabling options and options that determine slot/stat gaining methods. This should make server config more consistent and less confusing. New options are available in _general_ section, under _*GainingMethod_ keys. Possible settings:
  - _None_ - gaining modifier slot/stat is disabled.
  - _Predefined order_ - modifier slot/stat on each level up will be awarded in a specific order. Reward can be previewed in level info tooltip. Check _*Order_ options in _general.slots/stats_ sections for further tuning.
  - _Random_ - modifier slot/stat awarded on each level up is randomized. Check _*RandomPool_ options in _general.slots/stats_ sections for further tuning.
- Experience value format in tooltips is now consistent with tool durability format.
### Added
- Support for stripping, scrapping, waxing off, tilling and path making actions. Added corresponding settings in server config.
- Added support for ranged weapons and corresponding config settings. Unfortunately, there is no way to add tool experience when the projectile hit the entity, so instead ranged weapons receive experience whenever projectile is released.
- Alternative suffix for level names (client config).
- Modifier icon.
- Modifier textures for Estoc and Rapier from [Tinkers' Rapier](https://www.curseforge.com/minecraft/mc-mods/tinkers-rapier) mod.
- Command to add/set level on held tool. Mostly for testing purposes, requires level 2 permission. 
  - _\tinkerslevellingaddon levels \<target\> add \[\<count\>\]_
  - _\tinkerslevellingaddon levels \<target\> set \<count\>_
- Command to add/set tool experience on held tool. Mostly for testing purposes, requires level 2 permission.
  - _\tinkerslevellingaddon xp \<target\> add \[\<count\>\]_
  - _\tinkerslevellingaddon xp \<target\> set \<count\>_
- Chinese translation (Thanks @3453890470).
### Fixed
- Armor slot types random pool default settings is using wrong value set.
- Tool tag in chat message on level up shows tool state before level up.
- Stat values display decimal part when it's not necessary.
- Typos in Polish and English translations.
- Typos in config comments.

## 1.1.0
### Added
- Option to enable/disable modifiers as level up reward.
- Option to receive tool/armor stats on level up (disabled by default).
- Option to randomize modifiers and stats on level up. This can be configured independently for tools and armor. You can define a pool from which the mod will draw modifier/stat, which allows you to influence the probability of receiving each modifier/stat.
- Level info tooltip. Shows modifiers and stats received so far and rewards for the next level. Next rewards are shown only if tool/armor does not yet reach max level and rewards are not set to random.
### Fixed
- Gaining experience by taking damage triggers too often.

## 1.0.1
### Fixed
- Breaking blocks yields experience regardless of tool type. Now only the tools that are effective on the block will receive experience.
- Dealing damage do not yield experience unless PVP is enabled and the target is a player. (Thanks @hickorysb !) 

## 1.0.0
- Mod released!