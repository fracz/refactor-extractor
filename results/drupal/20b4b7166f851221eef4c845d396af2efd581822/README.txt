commit 20b4b7166f851221eef4c845d396af2efd581822
Author: Dries Buytaert <dries@buytaert.net>
Date:   Thu Dec 6 17:33:05 2001 +0000

    - book.module:
       + Added (1) support for "PHP pages" (dynamic pages), and (2) made
         it possible to link other node types into the book's tree/outline.
         It works just fine, yet the only (obvious) downside of (2) is
         that the navigation tree/links gets "interrupted" when you view
         non-book pages in the book.
           [SQL update required, see update.php]

       + Tidied up the book table.
           [SQL update required, see update.php]

    - various updates:
        + Fine-tuned the new node system.
        + Updated the inline/code documentation.
        + Improved teaser handling of all node types.
        + Made several small usability improvements to the node admin
          pages.