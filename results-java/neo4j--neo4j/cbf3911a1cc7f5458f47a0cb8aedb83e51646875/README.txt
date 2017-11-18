commit cbf3911a1cc7f5458f47a0cb8aedb83e51646875
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Wed Sep 30 15:58:29 2015 +0200

    Uses highest committed transaction id as tx obligation to slaves

    this requires a good commit description, but we'll probably revert this
    commit anyway to refactor how getLastCommittedTransactionId() is used and
    implemented as a whole, so that can wait.