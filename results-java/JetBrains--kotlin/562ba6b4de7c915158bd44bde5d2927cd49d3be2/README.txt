commit 562ba6b4de7c915158bd44bde5d2927cd49d3be2
Author: James Strachan <james.strachan@gmail.com>
Date:   Thu Mar 8 11:58:02 2012 +0000

    further refactorings to work around kotlin compiler bugs (nested classes & access to public fields in java classes) so we can extend the KotlinCompiler from Kotlin code to add new arguments and compiler plugins