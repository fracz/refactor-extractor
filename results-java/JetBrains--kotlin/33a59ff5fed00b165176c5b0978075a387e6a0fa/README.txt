commit 33a59ff5fed00b165176c5b0978075a387e6a0fa
Author: Stepan Koltsov <stepan.koltsov@jetbrains.com>
Date:   Wed May 23 02:52:32 2012 +0400

    Name class

    In the most places in frontend identifier is stored in Name class, was in String.
    Name has two advantages over String:
    * validation: you cannot accidentally create identifier with dot, for example
    * readability: if you see String, you don't now whether it is
      identifier, fq name, jvm class name or something else

    Name's disadvantage is (small) performance overhead. We have no value types in JVM.