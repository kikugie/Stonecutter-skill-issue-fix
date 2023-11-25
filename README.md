# Stonecutter
Preprocessor/JCP inspired multi-version environment manager

## About
This gradle/intellij plugin is a manager that allows working on a project that targets multiple compatabile release versions of a dependency. This is mostly meant for Loom-based Fabric projects written in java.

The project is heavily inspired by [Preprocessor](https://github.com/ReplayMod/preprocessor) and [JCP](https://github.com/raydac/java-comment-preprocessor) and the way they work. 
While they are great, I had a few issues with them and so this bad boy was made! Huge credit goes to the developers of these projects for giving me the idea!

For the name Stonecutter, I have another gradle plugin named "`Fletching Table`" and both that plugin and this one are meant to complement Fabric's Loom plugin, you can follow that thinking on your own. 

(This project is experimental, use at your own risk and always back up stuff with vcs such as git)

## Features and setup
See the [wiki page](https://github.com/kikugie/Stonecutter-skill-issue-fix/wiki) for setup guide and features.

### Intellij IDEA plugin
**The plugin is currently not fully compatible with the fork**

Stonecutter also contains a complementing IDE plugin for Intellij based IDEs. The plugin is meant to ease the usage of Stonecutter but Stonecutter can also work without it.

To install the plugin:
1. Open the plugins section in Intellij IDEA(`Ctrl`+`Alt`+`S` → Plugins)
2. Click the gear button at the top
3. Click "Install Plugin from Disk..."
4. Go to the project folder and open `/.gradle/stonecutter/Stonecutter-<version>.jar` (if it's not there, refresh gradle first)

To use the plugin's editor, hit `Ctrl`+`Shift`+`S` while working on a stonecutter project.

... (section not finished)

# Changelog
Look at the [commits](https://github.com/kikugie/Stonecutter-skill-issue-fix/commits/main) for a changelog.

## Planned
- [ ] Custom condition parsers
- [ ] Better syntax error reports
- [ ] Cross-version remapper
- [ ] Intellij Integration: Add easy way of using the tokenizer
- [x] Make the Intellij IDEA plugin easier to install
- [x] Add more integration features to the Intellij IDEA plugin(switch versions, insert versioned code, etc..)
- [x] Proprocessor-style commenting formatter
- [x] Intellij IDEA integration
- [x] Chiseled Tasks
- [x] Regex based token "find and replace" system
