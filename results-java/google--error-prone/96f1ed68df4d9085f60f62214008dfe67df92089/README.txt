commit 96f1ed68df4d9085f60f62214008dfe67df92089
Author: andrewrice <andrewrice@google.com>
Date:   Fri Apr 7 15:25:35 2017 -0700

    Updated NamedParameters to improve error message when comment doesn't conform
    to style and also to detect bad after-block comments. Change the suggestion to include spaces around the equals sign (the check is agnostic to spaces).

    This change also removes @RequiresNamedParameters. We probably shouldn't be providing a new language feature which legitimizes an API design which should instead be refactored to improve usability.

    RELNOTES: Change suggestion for NamedParameters to include spaces around the equals sign, improved errormessage and removed @RequiresNamedParameters

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=158570586