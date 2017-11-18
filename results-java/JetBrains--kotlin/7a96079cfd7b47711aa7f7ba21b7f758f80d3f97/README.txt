commit 7a96079cfd7b47711aa7f7ba21b7f758f80d3f97
Author: James Strachan <james.strachan@gmail.com>
Date:   Sat Apr 14 05:26:23 2012 +0100

    kdoc improvements: added discovery of ReadMe.md or ReadMe.html files in a package source directory, so we can auto-discover documentation like this https://github.com/JetBrains/kotlin/blob/master/libraries/stdlib/src/kotlin/ReadMe.md and fixed a regression where we could not find the KPackage of a descriptor with changes to the AST