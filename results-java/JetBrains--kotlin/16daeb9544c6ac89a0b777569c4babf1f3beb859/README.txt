commit 16daeb9544c6ac89a0b777569c4babf1f3beb859
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Tue Apr 29 20:15:10 2014 +0400

    Misc refactorings in KotlinBuiltIns

    - delete fields which were supposed to be used for caching, but mistakenly were
      never used
    - delete/inline unused methods and constants
    - delete useless logic related to root package fragment: package fragment
      providers are only supposed to find packages they're aware of