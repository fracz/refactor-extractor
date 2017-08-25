commit 284abb0925b68e5f6fa6ebd467cb1dd201fa9800
Author: skodak <skodak>
Date:   Fri Sep 28 20:12:43 2007 +0000

    MDL-11495 grade letter setting improvements:
    * in_null for decimals defaults checks
    * store letter settings only in database - no cfg settings
    * separate configuration page for letters only - pluginselector+admin tree
    * new capability to manage grade letters - similar to scales and outcomes
    * added grade_letters db index
    * grade letters related minor bugfixing