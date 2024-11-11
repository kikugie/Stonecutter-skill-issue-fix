# Stonecutter comments

After finishing the Gradle setup, you can start using versioned comments in your code.

## Conditional comments
The [setup](./setup) guide creates four versions: `1.20.1`, `1.20.4`, `1.20.6` and `1.21-alpha.24.20.a`.
You can insert conditions based on those versions.

Conditions must start with the `?` character, followed by an expression. 
You can **optionally** include keywords like `if`, `else`, `elif` or `else if` before the expression to improve readability.

### Single-line blocks
::: tabs
== Enabled
```java [example.java]
//? if <1.20.5
method1();
```
== Disabled
```java [example.java]
//? if <1.20.5
/*method1();*/
```
:::

### Multi-line blocks
Multi-line conditions must be marked with `{` and be closed by another condition starting with `}`.
::: tabs
== Enabled
```java [example.java]
//? if <1.20.5 {
method1();
method2();
//?}
```
== Disabled
```java [example.java]
//? if <1.20.5 {
/*method1();
method2();
*///?}
```
:::

### Inline blocks
Stonecutter conditions don't have to start with a `//` comment, which allows inlining the statements:
::: tabs
== Enabled
```java [example.java]
method1(/*? if <1.20.5 {*/"param"/*?} */);
```
== Disabled
```java [example.java]
method1(/*? if <1.20.5 {*//*"param"*//*?} */);
```
:::
You can also use the `>>` marker to only comment the next non-empty string:
::: tabs
== Enabled
```java [example.java]
method1(/*? if <1.20.5 >>*/ "param" );
```
== Disabled
```java [example.java]
method1(/*? if <1.20.5 >>*/ /*"param"*/ );
```
:::


### Else clauses
If you want an else clause in your statement, all conditions before it must be multi-line.

::: tabs
== Enabled
```java [example.java]
//? if <1.20.5 {
method1();
//?} else
/*method2();*/
```
== Disabled
```java [example.java]
//? if <1.20.5 {
/*method1();
*///?} else
method2();
```
:::
::: info Note
The following is **invalid syntax**. Even if the first condition is a single line,
it must use multi-line syntax before the else clause.
```java
//? if <1.20.5
method1();
//? else
/*method2();*/
```
:::

### Condition chains
Writing a condition after `}` can be used to add another branch. 
It can also be prefixed with `else`, `elif` or `else if` for readability.

::: tabs
== Enabled
```java [example.java]
//? if <1.20.5 {
method1();
//?} elif 1.20.6 {
/*method2();
*///?}
```
== Disabled
```java [example.java]
//? if <1.20.5 {
/*method1();
*///?} elif 1.20.6 {
/*method2();
*///?}
```
:::

### Nested conditions
Conditional blocks can be nested within each other **up to 10 times**.
To avoid issues with multi-line comments, `/* */` replaced with `/^ ^/`,
which may also include superscript numbers to correctly manage the blocks.
::: tabs
== Enabled
```java [example.java]
//? if <1.21 {
method1();
    //? if <1.20 {
    method1();
        //? if <1.19 {
        method1();
        //?}
    //?}
//?}
```
== Disabled
```java [example.java]
//? if <1.21 {
/*method1();
    //? if <1.20 {
    /^method1();
        //? if <1.19 {
        /^¹method1();
        ¹^///?}
    ^///?}
*///?}
```
:::

### Version predicates
Stonecutter supports a variety of version comparison predicates:
- `=`, `<`, `>`, `<=`, `>=` - basic equality checks.
- `~` - checks if the major and minor versions are equal. (I.e. `1.2.3 ~1.2.1`)
- `^` - checks if the major versions are equal. (I.e. `1.2.3 ^1.4`)
- ` ` - (no operator) same as the `=` check. (So you can write `if 1.20` instead of `if =1.20`)

Stonecutter **doesn't support** the following SemVer predicates:
- `x` - (as in `1.20.x`) these can be replaced with `~` or `^` operator for most cases.
- `*` - ('any' predicate) useless for Stonecutter, as such condition would always succeed and should be removed in that case.

### Combined predicates
Version predicates can be directly combined in conditions:
::: tabs
== Enabled
```java [example.java]
//? if <1.21 >1.20.4
method1();
```
== Disabled
```java [example.java]
//? if <1.21 >1.20.4
/*method1();*/
```
:::

## Condition constants
Stonecutter allows providing values to be used in the comment syntax. 
This is especially useful if you're making a multi-platform build.

For example, let's add `fabric`, `forge` and `neoforge` constants.
Constant keys may only contain English letters, digits, `_` and `-`.

::: info Multi-platform notice
These loaders are used only as an example here.
A proper multi-platform setup will be covered in a separate article,
please refer to it for the best configuration practices.
:::

::: code-group
```kotlin [stonecutter.gradle.kts]
stonecutter parameters {
    val loaders = listOf("fabric", "forge", "neoforge")
    val current = "fabric" // Acquire it dynamically, for example from `property("loom.platform")`
    
    // Constants should be given a key and a boolean value
    const("fabric", loader == "fabric")
    const("forge", loader == "forge")
    const("neoforge", loader == "neoforge")
    
    // When you have a list of choices, the following method is more convenient to use
    consts(current, loaders)
}
```
```groovy [stonecutter.gradle]
stonecutter.parameters {
    def loaders = ["fabric", "forge", "neoforge"]
    def current = "fabric" // Acquire it dynamically, for example from `property("loom.platform")`
    
    // Constants should be given a key and a boolean value
    const("fabric", loader == "fabric")
    const("forge", loader == "forge")
    const("neoforge", loader == "neoforge")
    
    // When you have a list of choices, the following method is more convenient to use
    consts(current, loaders)
}
```
```kotlin [build.gradle.kts]
val loaders = listOf("fabric", "forge", "neoforge")
val current = "fabric" // Acquire it dynamically, for example from `property("loom.platform")`
stonecutter {
    // Constants should be given a key and a boolean value
    const("fabric", loader == "fabric")
    const("forge", loader == "forge")
    const("neoforge", loader == "neoforge")

    // When you have a list of choices, the following method is more convenient to use
    consts(current, loaders)
}

```
```groovy [build.gradle]
def loaders = ["fabric", "forge", "neoforge"]
def current = "fabric" // Acquire it dynamically, for example from `property("loom.platform")`
stonecutter {
    // Constants should be given a key and a boolean value
    const("fabric", loader == "fabric")
    const("forge", loader == "forge")
    const("neoforge", loader == "neoforge")

    // When you have a list of choices, the following method is more convenient to use
    consts(current, loaders)
}
```
:::

These constants will be available in the comment snippets in your source code.

### Basic checks
Constant checks are no different to the version predicates and can use the same syntax as in the previous section.
::: tabs
== Enabled
```java [example.java]
//? if fabric
fabricMethod();
```
== Disabled
```java [example.java]
//? if fabric
/*fabricMethod();*/
```
:::

### Expanded conditions
::: info Combined checks
Constant checks can't be combined the same way version predicates do.
This makes statements like `if 1.20 fabric` invalid.
:::

Constants and version predicates can be combined with a handful of operators:
- `!` - negation: `if !fabric`, `if !=1.20`
- `||` - union (or): `if fabric || forge`, `if 1.20 || 1.21`
- `&&` - intersection (and): `if 1.20 && fabric`
- `( )` - grouping: `if fabric && (1.20 || 1.21)`

### Type resolution
Stonecutter allows setting the default (Minecraft) version to non-semantic value.
For example, with `versions("fabric", "forge", "neoforge")`. 
This may cause issues if you have constants with the same names.

The following sections explain some of the edge cases that may occur
and the logic the parser uses to resolve them.

::: details Numeric constants
```java [example.java]
//? if 1.0
```
If `1.0` is a constant, declared with `const("1.0", true)`, **it will not be recognized as such**.
If a string is a valid semantic version, it will **always** be treated as a predicate.
:::
::: details Explicit predicates
```java [example.java]
//? if =const && const
```
`=const` is treated as a string version, whereas `const` is a defined constant.
This way, comparison operators can be used to mark a string as a predicate 
if normally it would have been recognized as a constant.
:::
::: details Chained predicates
```java [example.java]
//? if >1.20 const
```
`>1.20` is recognized as a semantic version predicate, which can have other predicates following it.
Since there's no boolean operator, such as `&&` or `||`, `const` is treated as a second predicate.
:::

## Condition dependencies
When you write `if <1.20`, what does `<1.20` check against?
Naturally, the Minecraft version you specified in `settings.gradle[.kts]` for this subproject.
However, you can also explicitly check using the `:` operator - `if minecraft: <1.20`.

Similarly, you can add your own targets.
The key may only contain English letters, digits, `_` and `-`,
and the given version must be a valid semantic version.
::: code-group
```kotlin [stonecutter.gradle.kts]
stonecutter parameters {
    val sodium = "0.5.10"
    dependency("sodium", sodium)
}
```
```groovy [stonecutter.gradle]
stonecutter.parameters {
    def sodium = "0.5.10"
    dependency("sodium", sodium)
}
```
```kotlin [build.gradle.kts]
val sodium = "0.5.10"
stonecutter {
    dependency("sodium", sodium)
}

```
```groovy [build.gradle]
def sodium = "0.5.10"
stonecutter {
    dependency("sodium", sodium)
}
```
:::

### Dependency checks
With the Sodium target declared, you can use it in version predicate checks:
::: tabs
== Enabled
```java [example.java]
//? if sodium: <0.6
sodium5Method();
```
== Disabled
```java [example.java]
//? if sodium: <0.6
/*sodium5Method();*/
```
:::

### Dependency overrides
With the `dependency()` function you can also override the Minecraft dependency Stonecutter uses.
Note that this may lead to some user errors, so use it only if this is absolutely necessary.

```kotlin [stonecutter.gradle[.kts]]
stonecutter parameters {
    dependency("minecraft", "2.0") // MINECRAFT TWO!!! HOW?!
}
```
```kotlin [build.gradle[.kts]]
stonecutter {
    dependency("minecraft", "2.0") // MINECRAFT TWO!!! HOW?!
}
```

## Value swaps
While writing versioned code, you may need to modify the same fragment in many places,
which adds a lot of boilerplate.

Swaps allow evaluating the condition before processing code and inserting the required code block.
The following condition will be used as an example:
```java [example.java]
//? if <1.20.5 {
method1();
//?} else
/*method2();*/
```

The options can be declared as swap values instead:
::: code-group
```kotlin [stonecutter.gradle.kts]
stonecutter parameters {
    // You can still call `swap(key, value)` as in Groovy, but Kotlin provides more convenient syntax
    swaps["my_swap"] = if (eval(stonecutter.current, "<1.20.5"))
        "method1();" else "method2();"
}
```
```groovy [stonecutter.gradle]
stonecutter.parameters {
    swap("my_swap", eval(stonecutter.current, "<1.20.5") ? "method1();" : "method2();")
}
```
```kotlin [build.gradle.kts]
stonecutter {
    // You can still call `swap(key, value)` as in Groovy, but Kotlin provides more convenient syntax
    swaps["my_swap"] = if (eval(stonecutter.current, "<1.20.5"))
        "method1();" else "method2();"
}

```
```groovy [build.gradle]
stonecutter {
    swap("my_swap", eval(stonecutter.current, "<1.20.5") ? "method1();" : "method2();")
}
```
:::

### Usage
Swaps can be inserted in the source code by starting a comment with the `$` marker.
```java [example.java]
//$ my_swap
method1();
```

Similar to conditions, multi-line replacements need a closing comment.
```java [example.java]
//$ my_swap {
method1();
//$}
```