commit 62fa20fd6f4b3ebbc2eaf47ebeb5d9f2d91f21c2
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Wed Aug 2 16:31:06 2017 +0200

    PathPattern#matchAndExtract minor refactoring

    Consistent behavior with matches(PathContainer), the two had slightly
    different logic for handling of empty paths.

    Make matchAndExtract independantly usable without the need to call
    matches(PathContainer) first. Essentially no longer raising ISE if the
    pattern doesn't match but simply returning null.