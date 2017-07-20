commit 1887ba80187318c51d32553bbfaec2186d15e6a6
Author: Dries Buytaert <dries@buytaert.net>
Date:   Tue Jun 13 09:42:58 2000 +0000

    Hoeray!  I have a first core version of submission moderation up and
    running.  This means people can submit stories, and moderators can
    moderate stories.  When a submission reaches a certain positive
    threshold (currently set to 2) the submission becomes a story and up
    it goes.   If a submission reaches a certain negative threshold
    (currently set to -2) the submission is dumped.

    The fact this is all done by our visitors (without our intervention)
    makes it truly spiffy imho.  The website can live a life on it's own,
    fed by the visitors.

    Beware, a lot of work need to be done though ... it's just a first
    basic implementation with the core functionality.  There are quite
    a lot of things that I'll need to change, extend and improve.  But
    comments, suggestions and ideas are - as always - welcomed.

    Please read this log message carefully!  It features quite a lot of
    important information.

    To test the moderation, log in, select theme 'Dries' (the other themes
    need a small update) and head by clicking the one and only 'submission
    moderation' link.  Don't be afraid to submit lame/funny/useless
    stories for testing purpose ... as soon we go public, we'll wipe out
    the story database.  ;-)


    WHAT'S NEW?
    -----------
    * Added 2 new operations to user.class.php to set and retrieve the
      user's "history".  Very evil but required to avoid people voting
      twice.
    * Moved dbsave() from account.php to functions.php.  In addition, I
      added a new function to user.class.php called `save()' that ...
      well, saves the object back to the database.  It's (IMHO) a better
      approach compared to dbsave(): it keeps things organized. ;-)


    BUGFIXES:
    ---------
    * Fixed a (heavy) memory leak in the constructor of user.class.php:
      mysql_fetch_array() returns an _associative_ array which made the
      constructor `pollute' the object with a lot of useless variables.
    * Fixed the slash-problem on the account pages. :-)
    * Fixed UnConeD's theme glitch, alas the warning.
    * Fixed the e-mail address not showing in the confirmation email
      (upon registration).
    * Fixed the typical quote and backslash problems in submit.php.
    * submit.php now uses the database abstraction layer.

    IMPORTANT:
    ----------
    * You can check the new submission system at:
        http://beta.drop.org/submission.php
      or by following the `submission moderation' link from my theme.
    * UnConeD, Jeroen: you'll need to update your themes to take
      advantage of the new function: displayAccount().  This function
      will display the `submission moderation' link when a user is
      logged on.
    * Natrak: you might want to apply the patches in user.class.php
      on the other sites using the same user-system.