# CHANGELOG
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