commit 01d9e786d83b48b008c88e2e3f460986f810a56f
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Sun Apr 15 21:28:46 2012 +0200

    improvements and fixes to DaemonForkOptions/ForkOptions/GroovyForkOptions

    - made CompilerDaemonManager respect jvmArgs
    - removed obsolete code from ForkOptions
    - added GroovyForkOptions.jvmArgs (necessary in order to be able to debug ApiGroovyCompiler)
    - made DaemonForkOptions retain order of several list-like properties (by using LinkedHashSet instead of HashSet)
    - added tests
    (cherry picked from commit 9c41ddbf427d7c0617b549913eef80b9ff300139)