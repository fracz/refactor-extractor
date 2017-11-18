commit 30060b60833d099f8ebefdf8174755284f2716d8
Author: nmittler <nmittler@gmail.com>
Date:   Mon Oct 20 10:19:54 2014 -0700

    Optimize IntObjectHashMap handling of negative keys.

    Motivation:

    The hashIndex method currently uses a conditional to handle negative
    keys. This could be done without a conditional to slightly improve
    performance.

    Modifications:

    Modified hashIndex() to avoid using a conditional.

    Result:

    Slight performance improvement to hashIndex().