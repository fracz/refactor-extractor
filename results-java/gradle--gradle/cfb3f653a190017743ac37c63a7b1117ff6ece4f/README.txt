commit cfb3f653a190017743ac37c63a7b1117ff6ece4f
Author: Adam Murdoch <a@rubygrapefruit.net>
Date:   Tue Sep 1 08:49:29 2009 +0000

    GRADLE-614, GRADLE-370
    - Use ASM instead of Groovy to generate subclasses, to improve evaluation time.
    - Kept the groovy backed ClassGenerator (for now)
    - Ported DefaultRepositoryHandler to java to work around groovy weirdness.

    git-svn-id: http://svn.codehaus.org/gradle/gradle-core/trunk@1813 004c2c75-fc45-0410-b1a2-da8352e2331b