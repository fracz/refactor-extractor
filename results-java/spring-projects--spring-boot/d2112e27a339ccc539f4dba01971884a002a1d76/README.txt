commit d2112e27a339ccc539f4dba01971884a002a1d76
Author: Dave Syer <dsyer@gopivotal.com>
Date:   Thu May 15 13:10:19 2014 +0100

    Add FreeMarkerProperties instead of raw Environment access

    It's better for readability and tooling. Changed
    templateLoaderPath -> path (simpler and unlikely to clash)