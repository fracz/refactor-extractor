commit 15f55d852d1a40ebf7a4af2cd64c799b8ef956f8
Author: Dmitry Jemerov <yole@jetbrains.com>
Date:   Mon May 30 20:35:58 2011 +0400

    first step of refactoring code to generate 'this' expressions and receivers in ExpressionCodegen; correctly generate references to outer class properties in inner superclass constructor call