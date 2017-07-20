commit 3958a714cda4b04ab9bfa24c13711158ae45d17c
Author: Dries Buytaert <dries@buytaert.net>
Date:   Thu Mar 8 08:16:23 2001 +0000

    - some important changes to our theme API which can be summerized as:

         abstract() + article() = story()

      abstract() and article() have been merged into a new function story()
      which looks like:

      function story($story_object, $reply) {
        if (!reply) {
           // full story
        }
        else {
           // main page version / abstract
        }
      }

      This should allow you to "compress" your theme as abstract() and
      article() tended to be 98% identical.

      => I didn't really merge your themes so I leave it up to *you* to
         improved the code!!! Do it ASAP as we release drupal 2.00 in 7
         days.

      In future we'll have similar functions for other content types as
      for example:

         review($review, $reply);
         enquete($enquete, $reply);
         ...