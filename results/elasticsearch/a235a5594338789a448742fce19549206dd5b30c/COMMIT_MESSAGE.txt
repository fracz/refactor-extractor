commit a235a5594338789a448742fce19549206dd5b30c
Author: Martijn van Groningen <martijn.v.groningen@gmail.com>
Date:   Tue Jul 30 17:06:55 2013 +0200

    Improved how aliases are handled in the cluster state.

    The following changes improved alias creation:
    * Moved away from ImmutableMap to JCF's UnmodifiableMap. The ImmutableMap always made a copy, whereas the UnmodifiableMap just wraps the target map.
    * Reducing the number of maps being created during the creation of MetaData and IndexMetadata.
    * Changed IndexAliasesService's aliases from a copy on write ImmutableMap to ConcurrentMap.

    Closes #3410