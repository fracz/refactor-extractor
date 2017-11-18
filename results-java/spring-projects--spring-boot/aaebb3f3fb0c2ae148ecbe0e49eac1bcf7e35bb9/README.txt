commit aaebb3f3fb0c2ae148ecbe0e49eac1bcf7e35bb9
Author: Phillip Webb <pwebb@vmware.com>
Date:   Mon Aug 5 15:18:55 2013 -0700

    Improve Ivy resolution

    Refactor GrapeEngineCustomizer to improve ivy resolution times by
    failing early when resolution is known to fail.