commit 7f6e27f1354db41fbf986fd2e3abcb770988323e
Author: ulfjack <ulfjack@google.com>
Date:   Tue May 30 22:51:38 2017 +0200

    Simplify the remote worker

    - Reduce scope of try-catch blocks
    - Handle non-zero exit the same as zero exit
    - Basic infrastructure to handle time outs, currently hard-coded to 15 mins

    Some of this code is copied from LocalSpawnRunner. Ideally, we'd reuse that
    implementation instead of writing yet another one, but that will have to wait
    for some more refactoring.

    PiperOrigin-RevId: 157506934