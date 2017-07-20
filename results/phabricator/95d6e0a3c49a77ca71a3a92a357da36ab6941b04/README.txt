commit 95d6e0a3c49a77ca71a3a92a357da36ab6941b04
Author: Ricky Elrod <ricky@elrod.me>
Date:   Fri Jul 15 16:49:45 2011 -0400

    Store parents of forked pastes, and list child pastes if there are any.

    Summary:
    Added a 'parent' field which stores a PHID of another paste. If it is not NULL show a list of children pastes on view.
    Also did some misc. refactoring to clean up the code a bit, specifically in the Create controller.

    Test Plan:
    - Checked old pastes, they were not affected.
    - Added a paste, successfully.
    - Forked it, successfully.
    - Went to the original paste, saw the child paste listed.
    - Forked it again, saw the new one added to the list.

    Reviewers:
    epriestley

    CC:

    Differential Revision: 672