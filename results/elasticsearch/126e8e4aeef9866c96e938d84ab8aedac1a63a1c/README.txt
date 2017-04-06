commit 126e8e4aeef9866c96e938d84ab8aedac1a63a1c
Author: Jason Tedor <jason@tedor.me>
Date:   Wed Aug 19 21:32:15 2015 -0400

    Improve Java version comparison in JarHell

    This commit improves Java version comparison in JarHell.

    The first improvement is the addition of a method to check the version
    format of a target version string. This method will reject target
    version strings that are not a sequence of nonnegative decimal integers
    separated by “.”s, possibly with leading zeros (0*[0-9]+(\.[0-9]+)?).
    This version format is the version format used for Java specification
    versioning (cf. Java Product Versioning, 1.5.1 Specification Versioning
    and the Javadocs for java.lang.Package.)

    The second improvement is a clean method for checking that a target
    version is compatible with the runtime version of the JVM. This is done
    using the system property java.specification.version and comparing the
    versions lexicograpically. This method of comparison has been tested on
    JDK 9 builds that include JEP-220 (the Project Jigsaw JEP concerning
    modular runtime images) and JEP-223 (the version string JEP). The class
    that encapsulates the methods for parsing and comparing versions is
    written in a way that can easily be converted to use the Version class
    from JEP-223 if that class is ultimately incorporated into mainline JDK
    9.

    Closes #12441