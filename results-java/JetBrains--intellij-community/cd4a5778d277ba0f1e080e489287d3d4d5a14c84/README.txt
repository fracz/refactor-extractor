commit cd4a5778d277ba0f1e080e489287d3d4d5a14c84
Author: Eugene Zhuravlev <jeka@intellij.com>
Date:   Fri Mar 2 23:09:32 2012 +0400

    - refactored the way "outputs to delete" are stored: in project-specific caches directory, loadable on demand;
    - projectId and moduleId symbol table not dependent on 'names' table from VFS