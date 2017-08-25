commit 286f97a1438c549ce2bcc0ed8dd6a56021b27231
Author: Mike van Riel <mike.vanriel@naenius.com>
Date:   Mon Feb 18 16:04:51 2013 +0100

    Add inheritance to the Descriptors

    Serializing seems to take references into account and this means we can add indexes and
    links to parent classes or interfaces by adding references to those indexes. This commit
    will add those capabilities and refactor the Reflector builder to allow easy coupling.