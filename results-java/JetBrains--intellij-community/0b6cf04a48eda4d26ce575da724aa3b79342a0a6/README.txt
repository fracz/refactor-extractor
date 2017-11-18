commit 0b6cf04a48eda4d26ce575da724aa3b79342a0a6
Author: peter <peter@jetbrains.com>
Date:   Tue Oct 10 09:52:47 2017 +0200

    less expensive CompositeElement.assertThreading

    call only at the top level to avoid O(depth^2) time

    improves PHP indexing (WI-29998), because AST modifications
    with subtreeChanged are performed routinely while parsing multi-root files
    and inserting outer language elements