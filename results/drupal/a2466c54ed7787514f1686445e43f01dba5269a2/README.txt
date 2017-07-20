commit a2466c54ed7787514f1686445e43f01dba5269a2
Author: Dries Buytaert <dries@buytaert.net>
Date:   Thu Jun 22 18:18:06 2000 +0000

    * Commited a *temporary* version of the new comment system: I have been
      working on it for about 4 a 5 hours today and I considered it would be
      smart (backup- or crash-wise) to commit what I have made so far.  I'm
      aware of a few bugs and I'll keep workin on it:
        - removing bugs
        - clean up the code to make it very streamlined
        - improve error checking
      Once we got a stable comment system, I'll add moderation.  But right
      now I want to sort out the major problems.
    * I made my theme the default theme until the other themes are updated.
    * Expanded the database abstraction layer with more goodies.