commit 91feae3c5994bd4768cea3507c62c65746adcfa6
Author: Svetoslav Ganov <svetoslavganov@google.com>
Date:   Thu May 19 18:16:31 2011 -0700

    TouchExplorer - refactoring and a couple of bug fixes

    1. Refactored the code to avoid code duplication.

    2. Fixed a bug in removing unused pointers from the event.

    3. Fixed a bug that was crashing the explorer.

    4. Sending hover exit immediately at the end of touch exploration
       gesture rather with a delay.

    Change-Id: Ie288cb8090d6fb5e5c715afa6ea5660b17c019e0