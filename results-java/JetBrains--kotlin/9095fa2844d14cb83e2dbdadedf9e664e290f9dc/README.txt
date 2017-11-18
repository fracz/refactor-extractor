commit 9095fa2844d14cb83e2dbdadedf9e664e290f9dc
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Fri Aug 7 16:00:22 2015 +0300

    Make PropertyMetadataImpl a data class

    To allow property delegates to use property metadata as a key in the hash map,
    and to improve debugging experience