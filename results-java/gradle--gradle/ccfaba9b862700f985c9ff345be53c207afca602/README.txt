commit ccfaba9b862700f985c9ff345be53c207afca602
Author: daz <darrell.deboer@gradleware.com>
Date:   Sun Dec 1 13:01:30 2013 -0700

    Replaced and refactored NativeLibraryDependency with NativeLibraryRequirement

    - NativeLibraryRequirement is not responsible for looking up Library, but has project path
    - NativeComponent has a BuildComponentIdentifier for obtaining project path
    - Refactored NativeDependencyResolver to split into:
       - LibraryBinaryLocator that provides set of candidate binaries for requirement
       - LibraryResolver which resolves a set of candidates to a single binary
       - DeferredResolutionLibraryNativeDependencySet which delays library lookup until required