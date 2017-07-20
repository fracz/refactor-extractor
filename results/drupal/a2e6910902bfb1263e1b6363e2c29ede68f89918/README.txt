commit a2e6910902bfb1263e1b6363e2c29ede68f89918
Author: Dries Buytaert <dries@buytaert.net>
Date:   Sat Nov 3 18:38:30 2001 +0000

    - Made the node forms support "help texts": it is not possible to configure
      Drupal to display submission guidelines, or any other kind of explanation
      such as "NO TEST POSTS", for example.

    - Added node versioning: it is possible to create revisions, to view old
      revisions and to roll-back to older revisions.  You'll need to apply a
      SQL update.

      I'm going to work on the book module now, so I might be changing a few
      things to enable collaborative, moderated revisions - but feel free to
      send some first feedback, if you like.

    - Added some configuration options which can be used to set the minimum
      number of words a blog/story should consist of.  Hopefully this will
      be usefull to stop the (almost empty) test blogs.

    - Various improvements:
       + Fine-tuned new node permission system.
       + Fine-tuned the functions in node.inc.
       + Fine-tuned some forms.
       + XHTML-ified some code.