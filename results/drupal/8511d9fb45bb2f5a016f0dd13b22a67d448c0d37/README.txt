commit 8511d9fb45bb2f5a016f0dd13b22a67d448c0d37
Author: Dries Buytaert <dries@buytaert.net>
Date:   Sat Jan 6 11:39:43 2001 +0000

    A batch of patches:

      - configuration:
        + renamed $db_name to $db_user
        + renamed $db_base to $db_name
      - fixed small diary glitch
      - fixed initial-comment-score problem
      - fixed comment rating bug: improved the API and updated the
        themes
      - removed some tabs from Steven ;)
      - fixed backend warnings and improved robustness
        I'm not happy yet with the headline grabber - it generates
        too many SQL errors.
      - some small cosmetic changes in comment.module
      - fixed minor glitch in format_interval()