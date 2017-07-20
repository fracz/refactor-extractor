commit 7af49ab274e7ea650abfecb323ae4b510960b5c3
Author: Dries Buytaert <dries@buytaert.net>
Date:   Tue Aug 12 18:32:54 2003 +0000

    - Committed Marco's comment module patch:

        + Dramatically improves performance of large discussions/threads: only
          very few SQL queries are required.
        + Replaces custom pager with standard pager.

      Modifications by me:

        + Reworded some code comments.
        + Removed dependencies on pager internals.