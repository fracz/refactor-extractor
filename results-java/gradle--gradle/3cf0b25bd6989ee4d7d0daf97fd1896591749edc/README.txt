commit 3cf0b25bd6989ee4d7d0daf97fd1896591749edc
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Fri Aug 2 09:05:52 2013 +0200

    ExternalResourceResolver improvements

    - use our own LatestStrategy and LatestMatcher
    - adapt to VersionMatcher now representing versions as Strings
    - use ModuleVersionSelector instead of ModuleVersionIdentifier where appropriate
    - internal rename refactorings
    - remove public methods related to (Ivy) LatestStrategy (also from LegacyDependencyResolver) (breaking DSL change)