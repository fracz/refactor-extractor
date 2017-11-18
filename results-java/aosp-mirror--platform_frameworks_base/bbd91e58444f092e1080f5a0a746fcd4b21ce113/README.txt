commit bbd91e58444f092e1080f5a0a746fcd4b21ce113
Author: Felipe Leme <felipeal@google.com>
Date:   Fri Feb 26 16:48:22 2016 -0800

    Make bugreport details dialog confirm to Material Guidelines for Dialogs.

    Changes:
    - Removed hints.
    - Added TextViews for field labels.
    - Added padding for inner dialog
    - Adedd autoCorrect and capSentences to title and summary
    - Changed strings.
    - Set name to be selectAllOnFocus initially.

    Also improved some logging statements.

    BUG: 26324085
    Change-Id: I32597a7c2839ca706dbbcf13660e976469ab8dd0