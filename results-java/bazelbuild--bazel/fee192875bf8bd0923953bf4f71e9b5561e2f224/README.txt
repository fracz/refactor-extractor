commit fee192875bf8bd0923953bf4f71e9b5561e2f224
Author: Nathan Harmata <nharmata@google.com>
Date:   Tue Feb 14 20:47:50 2017 +0000

    Some minor logging improvements related to diff awareness:

    (i) Log [the first 100] external [non external-repo] file paths when we encounter them in skyframe [when externalFileAction is not ASSUME_NON_EXISTENT_AND_IMMUTABLE_FOR_EXTERNAL_PATHS]. These caveats are to prevent log spam.

    (ii) Fix grammar when logging the results of handleDiffsWithMissingDiffInformation in the case when all package paths have known diff information and we're merely checking external/output/external-repo files.

    (iii) Log the purpose of each skyframe graph scan we do (see (ii)).

    --
    PiperOrigin-RevId: 147508404
    MOS_MIGRATED_REVID=147508404