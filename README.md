# plugin-native

### A plugin for the [ja-netfilter](https://github.com/ja-netfilter/ja-netfilter), it is a wrapper for native methods.

#### Required: `ja-netfilter >= 2.1.1`

#### Use the `mvn clean package` command to compile and use `native-vX.X.X-jar-with-dependencies.jar` file!

## Config file: `native.conf`

```
; wrap specified classes
[Class]
EQUAL,package/to/ClassName

# EQUAL       Use `equals` to compare
# EQUAL_IC    Use `equals` to compare, ignore case
# KEYWORD     Use `contains` to compare
# KEYWORD_IC  Use `contains` to compare, ignore case
# PREFIX      Use `startsWith` to compare
# PREFIX_IC   Use `startsWith` to compare, ignore case
# SUFFIX      Use `endsWith` to compare
# SUFFIX_IC   Use `endsWith` to compare, ignore case
# REGEXP      Use regular expressions to match
```
