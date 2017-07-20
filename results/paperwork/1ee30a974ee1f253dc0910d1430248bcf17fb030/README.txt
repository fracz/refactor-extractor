commit 1ee30a974ee1f253dc0910d1430248bcf17fb030
Author: Clément Lafaurie <grainderiz@melix.net>
Date:   Wed Jun 24 21:14:51 2015 +0200

    continuation on #282, refactoring notebook sidebar

    I used the angular-ui-bootstrap collapse directive to replace the bootstrap-tree.js code. I did not remove the bootstrap-tree.js from the repository, however il commented the corresponding line in the gulpfile, so that it is not used anymore.