# Versioned comments
To modify the source code to match your dependencies at compile time, 
Stonecutter uses its own code, written in comments.

Comment processing is managed by the Stitcher project (like tearing a file apart and *stitching* it together) - a transpiler for the comment syntax.
This guide won't dive into the process of parsing code, but in case I mention it, now you know.

## Comment types
Stonecutter comments are prefixed with special symbols, which mark them as code to be processed.

### Condition
Condition comments support all the functions of boolean algebra, such as:
```java
//? if bl1 || (bl2 && !bl3)
method();
```
The comment above will technically fail, since we haven't told Stonecutter what `bl1`, `bl2` and `bl3` are, 
but it shows the principle. If the condition fails, the function below will be commented, or otherwise the comment will be removed.

By default, the only available values are Minecraft version predicates, such as `>=1.20.1`, `<1.21`, `=1.20.6`, etc.

### Swap
Swaps replace the value with a predefined parameter.
```java
// 1.20.1
//$ swap_token
method1();

// 1.20.6
//$ swap_token
method2();
```
They’re replacements to doing the same with an if-else boilerplate:
```java
//? if <1.20.6 {
method1();
//?} else
/*method2();*/
```

The way to configure swaps is described in the next chapter.

## Comment scopes
You may have noticed a comment ending with `{` above, which is a part of defining a comment scope.  
Scope is the part of the code your versioned comment affects.
There are three types:
- Closed - section from a condition/swap ending with `{` to another condition/swap starting with `}`.
- Word - condition/swap ending with `>>`, targets the next uninterrupted sequence of characters.
- Line - empty end, targets the next non-empty line.

> [!IMPORTANT]
> If you are chaining conditions, only the last one is allowed to be non-closed.
> ```java
> //? if bl1 {
> /*method1();
> *///?} else
> method2();
> ```

### Closed
```
//$ swap {
this
all
is
included
//$}
```

### Word
```
//$ swap >>
included not_included
```

### Line
```
//$ swap
included included
not_included
```

## Comment types
Condition/swap comments can be written as single and multi-line comments:
``` 
//$ swap >>
replace
/*$ swap >>*/ replace
```
However, as of Stonecutter `0.4`, contents can only be commented with a multi-line comment.

## Nested comments
Handling nested comments is surprisingly challenging, with a big difference between Java and Kotlin:
```java
/* /* comment */ */
//   ends here ^
```

```kotlin
/* /* comment */ */
//      ends here ^
```
The comment scanner takes the Java approach, since it's simpler, 
but when processed comments are applied, Stonecutter takes neither side - there shall be no nested multi-line comments.
After being processed by Stonecutter the example above will look like this:
```
/* /^ comment ^/ */
```
Inner comments are replaced with `/^ ^/`. If Stonecutter needs to remove the outer comment, 
those placeholders will be converted to standard `/* */` comments.

## Syntax reference
The following values are reserved as syntax:  
`? $ { } ( ) ! && || >> if else elif 🍌`