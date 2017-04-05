commit aec284a4ca4db3adf6f7d8ae9da6ddc30ce367f1
Author: Sam Brannen <sam@sambrannen.com>
Date:   Wed Dec 10 22:59:00 2014 +0100

    Create empty EnumSets & EnumMaps in CollectionFactory

    SPR-12483 introduced automatic type conversion support for EnumSet and
    EnumMap. However, the corresponding changes in CollectionFactory
    contradict the existing contract for the "create approximate" methods
    by creating a copy of the supplied set or map, thereby potentially
    including elements in the returned collection when the returned
    collection should in fact be empty.

    This commit addresses this issue by ensuring that the collections
    returned by createApproximateCollection() and createApproximateMap()
    are always empty.

    Furthermore, this commit improves the Javadoc throughout the
    CollectionFactory class.

    Issue: SPR-12533