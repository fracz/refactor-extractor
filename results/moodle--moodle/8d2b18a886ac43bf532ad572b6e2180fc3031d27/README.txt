commit 8d2b18a886ac43bf532ad572b6e2180fc3031d27
Author: skodak <skodak>
Date:   Sun Jan 28 20:52:57 2007 +0000

    MDL-8337 has_capability() ignores default roles for $userid and does not detect changed $USER and other minor fixes (this is needed for global $COURSE cleanup and fixed+improved cron.php)