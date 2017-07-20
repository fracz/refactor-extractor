commit 3b47f83b768fce8eb92abd76e55448e773e983df
Author: sgiehl <stefan@piwik.org>
Date:   Tue Jan 21 14:01:46 2014 +0100

    improved general device detection: always assume that there are no letters before the searched regex as there are many incorrect detected devices otherwise (eg. searching for LG phones with 'LG' only also matched 'Coolgen E70')