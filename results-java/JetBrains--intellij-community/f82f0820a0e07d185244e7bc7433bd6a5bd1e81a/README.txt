commit f82f0820a0e07d185244e7bc7433bd6a5bd1e81a
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Thu Mar 3 18:33:17 2016 +0300

    [hg]: move BackgroundTaskUtil to platform-impl

    * reuse for hg appropriate command execution instead of simple execution on application PolledThread

    * style: refactor static methods signatures to be able call not only from AWT