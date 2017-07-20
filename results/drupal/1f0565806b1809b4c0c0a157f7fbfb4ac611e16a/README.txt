commit 1f0565806b1809b4c0c0a157f7fbfb4ac611e16a
Author: Dries Buytaert <dries@buytaert.net>
Date:   Sun Apr 15 17:01:32 2001 +0000

    - improved submit.php:
      it now uses the new category code, incl content bindings.
      You can setup different "categories" which map on a content
      type.  Example:
        review   -> review.module
        article  -> story.module
        column   -> story.module
        announc. -> story.module
        addons   -> file.module
        themes   -> file.module
    - "generalised" story.module and book.module's output.
    - fixed bug in includes/timer.inc
    - fixed glitch in theme example.theme: it said "$how by" but
      the variable $how has never been declared.
    - added "drupal development settings" to display some timings
    - more work on the categories/topics -> does NOT work yet