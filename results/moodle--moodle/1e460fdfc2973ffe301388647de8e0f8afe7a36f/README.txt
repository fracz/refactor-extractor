commit 1e460fdfc2973ffe301388647de8e0f8afe7a36f
Author: martinlanghoff <martinlanghoff>
Date:   Wed Sep 19 07:29:31 2007 +0000

    accesslib: remove references to deprecated context_rel table and insert_context_rel()

    These references to the deprecated functions were erroring out. Remove
    them.

    Note however that other role related cleanups done as part of
    MDL-10679 "improvement to context_rel table and load_user_capability()"
    are kept.