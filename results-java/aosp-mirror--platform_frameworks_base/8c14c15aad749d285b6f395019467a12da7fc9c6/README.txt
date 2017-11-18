commit 8c14c15aad749d285b6f395019467a12da7fc9c6
Author: Craig Mautner <cmautner@google.com>
Date:   Thu Jan 15 17:32:07 2015 -0800

    Replace waitingVisible and other refactors

    - ActivityRecord.waitingVisible is identical to
    ActivityStackSupervisor.mWaitingVisibleActivities.contains(). This
    ArrayList is never very large so much code can be simplified by
    eliminating the waitingVisible member.

    - The processStoppingActivityLocked() method can eliminate a lot of
    variables by traversing the list top down. This makes the code
    simpler to analyze and maintain.

    - Declarations of ArrayLists do not need parameterization in the new
    constructor. These have been removed in ActivityStackSupervisor.

    Fixes item #5 of bug 18088522.

    Change-Id: Ib9d648c5fa32c8dd7313882864886c929e1ebc21