commit 10e3b2b22b830f741440deef64f74e3066bc207f
Author: Lukacs Berki <lberki@google.com>
Date:   Tue Sep 22 07:58:20 2015 +0000

    Make TargetPatternResolver#{isPackage, getTargetsInPackage} take a PackageIdentifier instead of a String.

    This remarkably fiddly CL is a step towards making wildcards pattern work with remote repositories. I originally wanted to refactor findTargetsBeneathDirectory(), too, but it turns out that it's a much more complicated affair.

    --
    MOS_MIGRATED_REVID=103622420