commit 23a3db8bb6f24baf5da926d078819989d281b691
Author: Adrien Grand <jpountz@gmail.com>
Date:   Thu Aug 6 15:04:48 2015 +0200

    Speed up the `function_score` query when scores are not needed.

    This change improves the `function_score` query to not compute scores at all
    when they are not needed, and to not compute scores on the underlying query
    when the combine function is to replace the score with the scores of the
    functions.