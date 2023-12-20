# Stonecutter
Preprocessor/JCP inspired multi-version environment manager

> [!WARNING]
> This fork of SHsuperCM's Stonecutter is significantly different.  
> Read the wiki page for more information.

> [!IMPORTANT]
> Read the [WIKI](https://github.com/kikugie/stonecutter-kt/wiki)  
> *(Scientists discovered that an ability to read solves 99% problems in life)*

## Features
### Plugin
- [x] Chiseled (parallel) tasks
- [x] Custom buildscript support
- [ ] Separate directory and version definitions
- [ ] Architectury support

### Processor
- [x] Comment formatter
- [x] Custom formatter conditions
- [x] Safe error handling
- [ ] Regex find-and-replace
- [ ] Versioned file patcher
- [ ] Nested comments

### Intellij IDEA Integration
*WIP and TBA*

## What's new?
- [Wiki](https://github.com/kikugie/stonecutter-kt/wiki)
- Kotlin support
- Custom buildscripts *(such as `build.gradle.kts`)*
- Improved comment syntax *(more readable, subjectively)*
- Informative errors *(imagine knowing the file and the line of the error, unbelievable)*
- Safe & faster file procesing *(it would be painful to find an error in comments after half the files have been processed, right?)*
- Custom conditions