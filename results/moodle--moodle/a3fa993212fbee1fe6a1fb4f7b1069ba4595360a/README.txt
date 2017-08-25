commit a3fa993212fbee1fe6a1fb4f7b1069ba4595360a
Author: Eloy Lafuente (stronk7) <stronk7@moodle.org>
Date:   Thu May 31 01:01:14 2012 +0200

    MDL-33434 mssql: Improve handling of returned ids on insert.

    This commit moves from the batch SCOPE_IDENTITY() way to return
    the inserted ids to the OUTPUT alternative (supported and
    recommended since 2005). Also the handling of possible return
    values is improved, with all the alternatives leading to expected results.