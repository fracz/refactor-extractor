commit 71aadaf321a345c6615a604231fda1f66ece11e8
Author: daz <darrell.deboer@gradleware.com>
Date:   Sun Apr 27 13:09:01 2014 -0700

    Moved ModuleComponentRepository.resolveArtifact() onto ModuleComponentRepositoryAccess

    - This makes things more consistent, and should simplify improvements to the implementations and caching