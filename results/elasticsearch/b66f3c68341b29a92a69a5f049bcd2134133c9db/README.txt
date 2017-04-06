commit b66f3c68341b29a92a69a5f049bcd2134133c9db
Author: Boaz Leskes <b.leskes@gmail.com>
Date:   Thu Sep 26 14:31:07 2013 +0200

    Send the update-mapping events before actually indexing into the shard, because the latter may generate exceptions (like when the shard is not yet ready to accept new docs).

    Also improved the SimpleDeleteMappingTest, as it make take a while until the update-mapping event is processed.

    Closes #3782