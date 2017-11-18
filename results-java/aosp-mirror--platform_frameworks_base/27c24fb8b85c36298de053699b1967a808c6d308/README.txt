commit 27c24fb8b85c36298de053699b1967a808c6d308
Author: Todd Kennedy <toddke@google.com>
Date:   Thu Sep 17 16:49:25 2015 -0700

    Enable "quick install"

    Quick install skips a lot of the normal install steps in order to
    dramatically reduce the installation time [eg Twitter normally takes
    20s to install. But, installs in under 2.5s with quick install]

    The specific optimizations [with caveats]:
    1. Use the JIT. Although the oat file is technically created, it
    only contains the exploded contents of the APK and does not contain
    pre-compiled native binary code. While this improves install time,
    it impacts app execution. [saves 17s]
    2. Bypass Play verification. Play normally verifies all installs
    to ensure we're not installing malware. But, it can take multiple
    seconds for Play to collect and send package information to our
    backend servers. [saves 2.7s]
    3. Reduce JAR file verification. Due to the structure of the JAR
    certs, we cannot completely bypass JAR processing. However we skip
    the step of verifying every manifest entry. [saves 1.3s]

    NOTE: #2 and #3 will only occur on eng/user-debug builds.

    Bug: 22848361
    Change-Id: I48e77595ad5c13a9534fdb06da67ba8dae2797fb