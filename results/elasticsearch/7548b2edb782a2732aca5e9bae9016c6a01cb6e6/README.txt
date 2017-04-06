commit 7548b2edb782a2732aca5e9bae9016c6a01cb6e6
Author: javanna <cavannaluca@gmail.com>
Date:   Wed May 14 14:17:45 2014 +0200

    Unified MetaData#concreteIndices methods into a single method that accepts indices (or aliases) and indices options

    Added new internal flag to IndicesOptions that tells whether aliases can be resolved to multiple indices or not.

    Cut over to new metaData#concreteIndices(IndicesOptions, String...) for all the api previously using MetaData#concreteIndices(String[], IndicesOptions) and removed old method, deprecation is not needed as it doesn't break client code.

    Introduced constants for flags in IndicesOptions for more readability

    Renamed MetaData#concreteIndex to concreteSingleIndex, left method as a shortcut although it calls the common concreteIndices that accepts IndicesOptions and multipleIndices