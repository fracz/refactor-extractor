commit 22f8ca4bee54f47f48a8bf3358f9ba9fd38a9322
Author: Andrey Breslav <andrey.breslav@jetbrains.com>
Date:   Wed Aug 15 12:29:07 2012 +0400

    resolveSupertypes() refactored

    * ClassDescriptor passed explicitly, psi-based checks and trace reading eliminated
    * some clarifying renames/extractions made