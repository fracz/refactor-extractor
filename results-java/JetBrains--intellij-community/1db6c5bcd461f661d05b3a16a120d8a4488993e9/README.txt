commit 1db6c5bcd461f661d05b3a16a120d8a4488993e9
Author: peter <peter@jetbrains.com>
Date:   Sat Feb 28 16:14:39 2015 +0100

    InProcessGroovyc refactoring

    extract classloader creation
    fix stream flush from non-groovyc thread
    allow jar locking and reuse for more efficient classloading