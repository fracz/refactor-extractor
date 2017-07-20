commit b6f1c9c739c6fe15bf3a7dffe5d5bfd20ec4a948
Author: Dries Buytaert <dries@buytaert.net>
Date:   Sun Nov 4 15:57:43 2001 +0000

    - node system:
        + fixed a typo in node_load(): it should be faster now

    - book module:
        + removed the functions book_parent() and book_parent_query() as
          they were no longer needed.  Gerhard & co: this should fix the
          occasional SQL errors you get, and should improve performance.

        + made the "next", "previous" and "up" links work correctly ...

        + XHTML-ified the code

        + added some missing translations

      I'm working on the book module now to make it possible to update book
      pages.