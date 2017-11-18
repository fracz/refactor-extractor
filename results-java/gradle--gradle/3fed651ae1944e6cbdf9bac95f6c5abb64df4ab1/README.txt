commit 3fed651ae1944e6cbdf9bac95f6c5abb64df4ab1
Author: daz <darrell.deboer@gradleware.com>
Date:   Tue Feb 11 17:03:20 2014 -0700

    Moved responsibility for selecting best match for a dynamic version out of ModuleVersionRepository and into UserResolverChain
    - Further refactoring required:
      - Remove more ivy from ExternalResourceResolver
      - Fix for custom ivy resolvers
      - Break UserResolverChain up into parts

    Better UserResolverChain