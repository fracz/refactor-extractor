commit 991ff265dad6480ab2632d61964ae0c0c27b29b0
Author: Stuart Douglas <stuart.w.douglas@gmail.com>
Date:   Wed Apr 15 14:03:39 2015 +1000

    HHH-10279 - Memory usage improvements -
    * Don't allocate IdentityMap if it is not needed
    * Only allocate unresolved insertions if requied
    * Don't create a new string when creating a named query
    * Allocate querySpaces lazily
    * Remove executableLists from ActionQueue
    * Allocate ExecutableList instances lazily
    * Lazily allocate the transaction commit task lists
    * Don't allocate a new EntityKey