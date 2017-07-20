commit b1c66cb5de4b37a5292c6901628b5b0697e8a459
Author: Dries Buytaert <dries@buytaert.net>
Date:   Mon Apr 16 18:21:22 2001 +0000

    Another big update so please read this carefully because there is important information hidden in it.

    Made it so that we can disable/enable comments on a category by category basis. In order to accomplish this I had to make a few (*temporary*) changes.

    I moved all comment code from the "module level" (eg. story.module) to the "node level".  It was nothing but the logical next step in nodifying drupal.  This enables us to add comments to all existing content types including book entries.  But also for book entries, this to can be toggled on and off. :-)

    Moreover module writers don't have to worry about the complex comment logic: it is "abstracted" away.  This implies that story.module got smaller, faster and easier to comprehend. :-)

    In order to accomplish this, I had to update ALL THEMES, which I did - and on my way I updated Goofy, Oranzh and UnConeD - with the previous changes.  All themes are up-to-date now!  I also had to remove the [ reply to this story ] links, and temporally re-introcuded the "Add comment" button in the "Comment control".  Tempora lly that is, UnConeD. ;)

    I plan to upgrade drop.org either tommorow or wednesday so test away if you have some time to kill. ;)

    Oh, I also fixed a few bugs and made various small improvements.