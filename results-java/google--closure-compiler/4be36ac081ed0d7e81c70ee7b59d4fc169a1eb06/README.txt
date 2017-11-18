commit 4be36ac081ed0d7e81c70ee7b59d4fc169a1eb06
Author: tbreisacher <tbreisacher@google.com>
Date:   Mon Apr 20 15:39:48 2015 -0700

    Fix a crash in JSDocInfoPrinter.

    At some point we should refactor so that function type nodes look more like regular function nodes (i.e. same number of children, no matter what, some of which are empty) so that we don't have to do so much work to find the paramList child. Then we can simplify all this, but for now I just want to make it not crash :)
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=91627785