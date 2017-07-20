commit e7019c257742aeeac1665682786131fb59733a4b
Author: Dries Buytaert <dries@buytaert.net>
Date:   Mon Oct 2 07:32:17 2000 +0000

    Changelog
    ---------
    - improved the user information page.
    - improved the story submission page.
    - fixed comments score bug: '.00' --> 'x.00'
    - tried fixing the calendar wrapping - UnConeD, is it fixed now?
    - provided a link back to the submission queue after having voted
      for a story.
    - fixed comment subject bug (and security flaw) by replacing
      quotes by &quot;.
    - updated theme 'zaphod': fixed 2 bugs.
    - updated theme 'marvin': fixed 1 bug and improved the layout so
      things wrap (hopefully) better in Windows.
    - comments have by default no subject pre-set - if no subject is
      provided, the user is warned and when a comment eventually got
      submitted without a subject, a subject is composed using the x
      first characters of the comment's body.
    - improved comments on submit.php
    - corrected a typo in the FAQ.

    UnConeD
    -------
    - replace 'article.php' by 'discussion.php'
    - comment() still uses old references to account.php: the
      parameters you supply to account.php does no longer hold.
      You have to update those links to the new syntax.
    - commentcontrol() is outdated - copy paste the one of
      theme 'marvin' and adjust it to your likings.