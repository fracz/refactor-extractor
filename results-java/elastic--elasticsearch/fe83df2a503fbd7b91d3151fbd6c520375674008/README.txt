commit fe83df2a503fbd7b91d3151fbd6c520375674008
Author: Jason Tedor <jason@tedor.me>
Date:   Thu May 18 15:12:46 2017 -0400

    Refactor update shard logic for primaries

    This commit is a simple refactoring of the update shard logic for
    primaries. Namely, there was some duplicated code here that was annoying
    to have to read twice so it is now collapsed with this commit.