commit dbad023be9ab48427813459449656fabc7abbd68
Author: mthvedt <mthvedt@google.com>
Date:   Tue Feb 3 07:06:58 2015 -0800

    Scan and preprocess package-info.java files to support package prefixes.

    I'm working on a refactor of FileProcessor, so that all scanning and sorting
    of input files is done before any work is done, but am pushing this now
    to unblock users who need package prefix functionality now.
            Change on 2015/02/03 by mthvedt <mthvedt@google.com>
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=85423078