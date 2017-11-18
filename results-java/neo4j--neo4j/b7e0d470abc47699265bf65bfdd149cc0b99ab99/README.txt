commit b7e0d470abc47699265bf65bfdd149cc0b99ab99
Author: Tobias Lindaaker <tobias@thobe.org>
Date:   Mon May 13 17:53:21 2013 +0200

    Provide configuration for an index to an index provider.

    Currently this configuration specifies if the index is unique or not (used by uniqueness constraints).

    Some refactorings around transaction logic, since we stumbled over it when browsing the code.