> [!WARNING]
> Expression blocks can't be nested and can't contain any documentation or multi-line comments, due to the Java/Kotlin compiler.

# Expression blocks
Each expression should start with `/*?` and end with `*/` forming a closed comment. You can have spaces around the condition.  
In original Stonecutter expressions must end with `?*/`. In this fork that question mark is not necessary, but if present it will be ignored for compatibility.

# Conditions
- Semver: `>1.20`, `=1.19.4`, `~1.17`  
  Compares active Minecraft version to the specified condition.  
  Version should follow [semantic versioning](https://semver.org/). For snapshots and pre-releases [Fabric's version normalization](https://github.com/FabricMC/fabric-loader/blob/master/minecraft/src/main/java/net/fabricmc/loader/impl/game/minecraft/McVersionLookup.java) is used.  
  The general pattern is `23w46a -> 1.20.3-aplha.23.46.a`, `1.20.3-pre2 -> 1.20.3-beta.2`, `1.20-rc1 -> 1.20-rc.1`. For edge cases and old versions refer to the normalizer source code.
- Else: `else`  
  Negates previous condition. Must follow another conditional block.
- Basic: `true`, `false`  
  Available only if `stonecutter.debug true` is set in `stonecutter.gradle`.  
  Predictably, always return their corresponding value.

# Expression types
> [!TIP]
> Whitespaces around conditions and brackets are optional.
> The only significant whitespace is between the keyword (`if`, `else`, `elif`) and the expression.

## Simple
Applied to the next line of code and doesn't require a closing block.  
Code can be either on the same or the next line.  
**Important:** the line of code means a real line, for example a method call with arguments put on separate lines will be treated as a multi-line.
```java
/*? if >1.20 */
func1();

/*? if >1.20 */ func1();
```

## Opener
Starts a multi-line block. For it to be qualified as an opener the expression must end with `{`.
```java
/*? if >1.20 {*/ // <- opener
func1();
func2();
/*?} */
...
```

## Extension
Adds a secondary condition similar to `else if`. Must start with `}` and end with `{`.
```java
/*? if >1.20 {*/
func1();
/*?} else {*//*
func2();
*//*?} */
```

With secondary conditions use `elif`.
```java
/*? if >1.20 {*/
func1();
/*?} elif <1.19 {*//*
func2();
*//*?} */
```

## Closer
Multi-line block must be closed with `/*?} */`.

# Custom expressions
Custom expressions should be defined in the buildscript and can use variables from it. The example uses Architectury Loom.  
**Please note that Architectury projects are not yet fully supported.**
```kt
val isForge = loom.isForgeLike
// addExpression(expr: (String) -> Boolean?)
stonecutter.addExpression() { loader ->
  return when(loader) {
    "fabric" -> !isForge
    "forge" -> isForge
    else -> null
  }
}
```
In this case the expression should return `true` or `false` if the expression is valid and `null` if it doesn't match.  
The null return is **especially important**. The parser goes through a list of defined expressions until one doesn't return a null. If null is not returned by your expression it may prevent other from executing.