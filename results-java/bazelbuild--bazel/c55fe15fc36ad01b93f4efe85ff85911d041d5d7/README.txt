commit c55fe15fc36ad01b93f4efe85ff85911d041d5d7
Author: Nathan Harmata <nharmata@google.com>
Date:   Tue Aug 2 18:13:28 2016 +0000

    Delete NodeEntryField since it's now superfluous in the presence of the new QueryableGraph.Reason which conveys more information. Add a few more Reason enum values to make this refactor benign.

    --
    MOS_MIGRATED_REVID=129118462