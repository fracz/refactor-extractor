commit d479b52d12fc782f18df6b5ae15c19e022f0ec14
Author: Calin Juravle <calin@google.com>
Date:   Wed Feb 24 16:22:03 2016 +0000

    Restrict the cases when we profile guided compile an apk

    Do not use profile guided compilation if the apk is loaded by another
    app. The decision if an apk was used or not by another app is done
    by looking into the foreign profile directory. Apks which where
    loaded in others apps will have a file marker in the profile directory.
    The marker is  named after the canonical location of the apk file where
    '/' is replaced by '@'.

    Also, refactor the profile paths to the Environment.

    Bug: 27334750
    Bug: 26080105
    Change-Id: Ic2ac5a7a231670ecb4462166c34fdd5b4c631178