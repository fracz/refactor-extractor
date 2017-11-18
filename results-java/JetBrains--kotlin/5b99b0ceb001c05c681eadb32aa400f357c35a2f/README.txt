commit 5b99b0ceb001c05c681eadb32aa400f357c35a2f
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Fri May 23 22:18:30 2014 +0400

    Make ClassBuilderOnDemand extend ClassBuilder

    Introduce DelegatingClassBuilder, refactor RemappingClassBuilder to use it