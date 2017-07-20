commit 7ce686c152416dc3a240861840f9c18791e38762
Author: Dries Buytaert <dries@buytaert.net>
Date:   Tue Dec 10 20:35:20 2002 +0000

    o Permission improvements:

       + Removed the "post content" permission and replaced it by more fine-grained permissions such as "maintain static pages", "maintain personal blog", "maintain stories", etc.

    o Usability improvements to teasers:

       + Teaser forms are no more.  Teasers are extracted automatically but can also be instructed using a delimiter "---".  Furthermore, when a post it too short for a teaser, the user won't be bother with teaser stuff anymore.

       + Added an option to set the teaser length, or to disable teasers all together.

       + When previewing a post, both the short (if any) and the full version of a post are shown.  This addresses a common complaint; for example, when writing a book page there was no way you could preview the short version of your post.

       + Forum posts can be teasered now.  This is particularly helpful in the context of drupal.org where we promote forum topics.

    o Bugfix: replaced all PHP short tags (<?) with long tags (<?php).

    o Bugfix: removed hard-coded dependence on comment module.

    o Bugfix: when the queue module was disabled, it was not possible to approve updated book pages.

    o Bugfix: applied modified version of Marco's node_teaser() fix.