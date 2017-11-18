commit 3d7b6eefd5d558d524039d4ed49f57064b8aeb84
Author: Chris Gioran <chris.gioran@neotechnology.com>
Date:   Tue Oct 4 16:55:41 2011 +0300

    Fixed DynamicArrayStore (via fixing ShortArray)  to properly handle 0 length arrays, even though we currently do not store them there
    Slightly refactored the corresponding test in TestDynamicStore and added another that tests for 0 length String arrays for good measure