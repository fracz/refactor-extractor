commit 378d327a3f8b0ac37dfe9f9e02590172d4bf51db
Author: Jim Miller <jaggies@google.com>
Date:   Wed May 30 03:19:43 2012 -0700

    Fix 6398209: SearchPanel gesture improvements

    This fixes a few recent regressions caused by other bug fixes:
    - add new flags to animateCollapse() so we can selectively close panels. Fixes regression caused by attempt to close recent apps from startAssistActivity() which had the side effect of closing the search panel before the animation completes.
    - adds tuneable holdoff delay for responding to home key press.
    - minor tweaks to MultiWaveView animations.

    Change-Id: Ia48434b8d59e7b0290a5e9783960c2f684068218