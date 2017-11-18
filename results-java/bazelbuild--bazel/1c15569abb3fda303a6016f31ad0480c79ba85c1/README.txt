commit 1c15569abb3fda303a6016f31ad0480c79ba85c1
Author: Nathan Harmata <nharmata@google.com>
Date:   Fri Apr 22 18:32:11 2016 +0000

    Fix target parsing bug with targets in the empty package (e.g. "blah" was incorrectly not being parsed as "//:blah").

    Also add tests for parsing absolute labels in the empty package.

    The empty package has been a thing in Bazel for a while now.

    Note that the old error message in this case "couldn't determine target for filename 'blah'" was almost always misleading and unhelpful since we were (almost certainly incorrectly) assuming the user intended for 'blah' to be an input file in the empty package. Now the error message would be "no such target '//:blah'") which is similarly misleading and unhelpful but probably marginally less so. If we desire to improve this, a future cleanup can introduce smarter error messages.

    --
    MOS_MIGRATED_REVID=120566819