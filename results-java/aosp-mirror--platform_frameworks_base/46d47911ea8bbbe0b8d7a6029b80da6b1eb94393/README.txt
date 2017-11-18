commit 46d47911ea8bbbe0b8d7a6029b80da6b1eb94393
Author: Felipe Leme <felipeal@google.com>
Date:   Wed Dec 9 13:03:09 2015 -0800

    Refactored what happens when a BUGREPORT_FINISHED is received.

    Previously on 24: when a BUGREPORT_FINISHED was received,
    BugreportProgressService would remove the watched BugreportInfo from its
    map and if there was no info left, it would stop self and send the
    SEND_MULTIPLE_ACTION intent.

    Soon we're going to allow the user to enter more details (like a title
    and description) for the bugreport, but if the service is stopped while
    the user is still entering data, that window will be killed.

    Hence, although this refactoring doesn't change the current logic, it
    paves the way for such new feature.

    BUG: 25794470

    Change-Id: Ic5283ddc3e07d88ba2a9a925f9534426857e7606