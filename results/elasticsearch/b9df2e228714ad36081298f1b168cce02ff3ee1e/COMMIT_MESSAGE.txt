commit b9df2e228714ad36081298f1b168cce02ff3ee1e
Author: Jason Tedor <jason@tedor.me>
Date:   Thu Dec 1 17:26:29 2016 -0500

    Improve the out-of-the-box experience

    Elasticsearch can be run in a few different ways:
     - from the command line on Linux and Windows
     - as a service on Linux and Windows

    on both 32-bit client and 64-bit server VMs. We strive for a great
    out-of-the-box experience any of these combinations but today it is
    lacking on 32-bit client JVMs and on the Windows service. There are two
    deficiencies that arise:
     - on any 32-bit client JVM we fail to start out of the box because we
       force the server JVM in jvm.options
     - when installing the Windows service, the thread stack size must be
       specified in jvm.options

    This commit attempts to address these deficiencies.

    We should continue to force the server JVM because there are systems
    where the server JVM is not active by default (e.g., the 32-bit JDK on
    Windows). This does mean that if a user tries to run with a client JVM
    they will see a failure message at startup but this is the best that we
    can do if we want to continue to force the server JVM. Thus, this commit
    at least documents this situation.

    To improve the situation with installing the Windows service, this
    commit adds a default setting for the thread stack size. This default is
    chosen based on the default thread stack size across all 64-bit server
    JVMs. This means that if a user tries to run with a 32-bit JVM they
    could otherwise see significantly higher memory usage (this situation is
    complicated, it's really only on Windows where the extra memory usage is
    egregious, but cutting into the 32-bit address space on any system is
    bad). So this commit makes it so that the out-of-the-box experience is
    improved for the Windows service on 64-bit server JVMs and we document
    the need to adjust this setting on 32-bit JVMs.

    Again, we are focusing on the out-of-the-box experience here and this
    means optimizing for the best experience on any 64-bit server JVM as
    this covers the vast majority of the user base. The users that are on
    32-bit JVMs will suffer a little bit but at least now any user on any
    64-bit server JVM can start Elasticsearch out of the box.

    Finally, we fix some references to the jvm.options documentation.

    Relates #21920