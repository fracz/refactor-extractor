commit 501cdbd8bb8ef9583b7d19892ef5adc87d7ff536
Author: martin <martin>
Date:   Wed Jul 31 14:19:35 2002 +0000

    OK, some massive changes with many files removed or changed.

    Basically the changes are:

     - I've merged the 'discuss' module into the forum module
       which makes the interface MUCH clearer for everyone
     - I've added a new 'single' forum type that replicates
       what the old discuss course modules used to look like.
     - I've got rid of the "discussion" forum type - it will
       still exist in upgraded courses but as a normal forum.
     - the 'discuss' module is completely deleted - gone.
     - the 'chat' module is completely deleted - gone.
     - The upgrading system has been improved, and all code
       is stored in version.php.
     - I've put in upgrading commands to do the best I can
       (right now) to upgrade courses that used the discuss
       module.  It should mostly work, just leaving some
       "orphan" coursemodules on you course front page.  You
       can easily delete these using the little 'x'.
       I may have forgotten something  - I've only tested on
       my testing server and I'm about to test on my production
       server to see how it goes.
     - Forums have a lot of little new features and fixes.  The
       main one is the subscription process.  Teachers can 'force'
       subscriptions on any forum.  This disallows everyone from
       choosing their own mail subscription - it's just on.
     - The assignment module is half-finished and not working yet

    I've still some massive changes to do, mostly involving making
    all the lib.php function names more standardised, so consider
    this is an interim checkin to do some tests.