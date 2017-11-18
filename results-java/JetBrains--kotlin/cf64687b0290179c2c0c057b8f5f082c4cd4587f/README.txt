commit cf64687b0290179c2c0c057b8f5f082c4cd4587f
Author: Svetlana Isakova <svetlana.isakova@jetbrains.com>
Date:   Sat Jun 27 14:26:03 2015 +0300

    More accurate error reporting
    with type inference error for delegated properties
    Add the constraints from completer if they don't lead to errors
    except errors from upper bounds to improve diagnostics