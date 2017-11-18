commit a4d1483792d6b8f85e983749e9f17d9385bd325d
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Sun Apr 15 21:42:19 2012 +0200

    improved fix for GRADLE-2232: TestCase not found on compile classpath in rc-1

    - became necessary because previous fix broke all transforms written in Groovy
    - copied and modified Groovy's JavaAwareGroovyCompilationUnit to solve the problem that this class doesn't allow to set transformLoader like CompilationUnit does (setting this field reflectively wouldn't work because it's already used in the super constructor)
    - changed ApiGroovyCompiler to use our JavaAwareGroovyCompilationUnit
    - added tests
    - modified samples/groovy/groovy-1.5.6 to use groovy(Test)Compile.groovyOptions.useAnt = true (ApiGroovyCompiler now requires Groovy 1.6 or higher)