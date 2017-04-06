commit 8949cb85fbac3e2209b5ca05936c8e2d009809c2
Author: Simon Willnauer <simonw@apache.org>
Date:   Tue May 19 22:24:02 2015 +0200

    [RECOVERY] Add engine failure on recovery finalization corruption back

    This engine failure on finalization corruption was lost on refactorings and
    should be added back.