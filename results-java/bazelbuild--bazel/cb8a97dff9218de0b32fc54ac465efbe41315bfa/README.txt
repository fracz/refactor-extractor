commit cb8a97dff9218de0b32fc54ac465efbe41315bfa
Author: Janak Ramakrishnan <janakr@google.com>
Date:   Thu Mar 23 20:50:08 2017 +0000

    Stop storing reverse deps to signal in BuildingState. Instead, re-use the reverseDepsToConsolidate field in InMemoryNodeEntry. As part of that, revamp our logic of how we store pending operations: store adds bare on initial evaluations, and checks bare on incremental evaluations and operations on done nodes.

    This should improve performance in two ways: BuildingState loses two fields, saving working memory intra-build. Storing pending reverse dep operations bare also saves memory intra-build. Note that neither of these changes helps resting memory state, only while a node is still evaluating.

    Because of this, we can simplify ReverseDepsUtil a bit, making ReverseDepsUtilImpl a static class, which it always wanted to be (what it really wants to be is a superclass of InMemoryNodeEntry, but I don't want to spend the object alignment bits).

    Finally, this makes it pretty tempting to get rid of BuildingState altogether on initial evaluations. We'd still keep DirtyBuildingState, but we could save another ~24 bytes by storing BuildingState's one remaining field, signaledDeps, directly inside InMemoryNodeEntry.

    --
    PiperOrigin-RevId: 151048879
    MOS_MIGRATED_REVID=151048879