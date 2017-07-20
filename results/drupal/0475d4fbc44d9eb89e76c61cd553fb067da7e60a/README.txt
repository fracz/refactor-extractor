commit 0475d4fbc44d9eb89e76c61cd553fb067da7e60a
Author: Dries Buytaert <dries@buytaert.net>
Date:   Mon Dec 30 12:03:53 2002 +0000

    Patch by Marco:

    - rewrote taxonomy_get_tree() for improved performance and cleaner code
    - fixed a bug in _taxonomy_term_select() with multiple parents
    - added hooks in vocabulary and term insert, update and delete
    - fixed a bug in taxonomy_save_vocabulary() (cache_clear_all() was never
    called)