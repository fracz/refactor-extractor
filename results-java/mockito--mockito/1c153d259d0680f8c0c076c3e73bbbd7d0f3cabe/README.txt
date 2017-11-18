commit 1c153d259d0680f8c0c076c3e73bbbd7d0f3cabe
Author: Lukasz Szewc <lukasz-szewc@users.noreply.github.com>
Date:   Fri May 8 22:27:22 2015 +0200

    Fixes #197 : Refactoring:
    - Removed unused field from VerificationOverTimeImpl and also from constructor's signature.
    - improved encapsulation by removing 3 getters from VerificationOverTimeImpl.
    - reduced visibility of few constructors in 'Timeout' and 'After' classes