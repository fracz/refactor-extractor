commit 8213f5b2627a6b63db9f84b572918bd7e3254dff
Author: Dries Buytaert <dries@buytaert.net>
Date:   Fri Apr 6 14:14:16 2001 +0000

    A lot of small changes (search-n-replace) make a big commit:

      - fixed update bug in book.module
      - provide a log message when both adding and updating book pages
      - all configurable variables are now accessed through "variable_get()":
      - rewrote watchdog and submission throttle and removed watchdog.inc
      - improved robustness of sections.inc
      - imporved story.module
      - updated ./database/database.sql