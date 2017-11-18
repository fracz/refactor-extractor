commit 420d5c34fe0261aaa9d338866b4056ce8c1f3c3f
Author: Ola Rozenfeld <olaola@google.com>
Date:   Wed Sep 14 02:31:22 2016 +0000

    Implementing directory Merkle trees for remote execution/caching (see
    https://docs.google.com/document/d/1hh63AzKlwcOJN6jBZzY3GNPffzww-JKx1015DfFKM6g/edit#).
    A directory is represented by recursive structure of TreeNodes, which are interned in a single TreeNodeRepository. Common subtrees are shared between various parents.

    In the change we introduce only creating the tree from an existing list of
    ActionInputs; in the future, we should refactor to populate the
    TreeNodeRepository alongside (or maybe even within) the ArtifactFactory.

    The plan is for the TreeNodeRepository to become a new centralized location for
    all Artifact data to be computed and cached.

    --
    MOS_MIGRATED_REVID=133080315