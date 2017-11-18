commit 69d85a28303e6fb3c26e7908f6b31ce836c6975d
Author: Rene Groeschke <rene@breskeby.com>
Date:   Mon Dec 17 10:24:17 2012 +0100

    Some more refactorings in coreImpl subproject:
    - Change ModuleVersionDescriptor.getId() to return ModuleVersionIdentifier instead of ModuleRevisionId
    - Change ModuleResolutionCacheEntry and DefaultModuleResolutionCache.RevisionKey to use ModuleVersionIdentifier instead of String
    - Change BuildableModuleVersionResolveResult.resolve to use ModuleVersionIdentifier.