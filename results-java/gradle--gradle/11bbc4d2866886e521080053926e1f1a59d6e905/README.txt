commit 11bbc4d2866886e521080053926e1f1a59d6e905
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Thu Mar 1 05:20:16 2012 +0100

    handle windows command line length limit problem for CommandLineJavaCompiler

    - use arguments file if command line gets too long
    - improvements to JavaCommandLineOptionsBuilder
    - print exit value of compiler for better diagnostics