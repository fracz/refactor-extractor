commit eed83c1596fd42fb99f051d6a22ecd0a7ea62b14
Author: bradfordcsmith <bradfordcsmith@google.com>
Date:   Wed Jun 14 08:58:36 2017 -0700

    CrossModuleCodeMotion: refactor to statement-based operation.
    This sets the stage for a performance fix that will make the pass smart enough
    to move all declarations to the best possible module in a single run.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=158983970