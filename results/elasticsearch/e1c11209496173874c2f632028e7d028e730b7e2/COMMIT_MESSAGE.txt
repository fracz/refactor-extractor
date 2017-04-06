commit e1c11209496173874c2f632028e7d028e730b7e2
Author: Adrien Grand <jpountz@gmail.com>
Date:   Thu Feb 6 15:16:34 2014 +0100

    Fix BytesRef owning issue in string terms aggregations.

    The byte[] array that was used to store the term was owned by the BytesRefHash
    which is used to compute counts. However, the BytesRefHash is released at some
    point and its content may be recycled.

    MockPageCacheRecycler has been improved to expose this issue (putting random
    content into the arrays upon release).

    Number of documents/terms have been increased in RandomTests to make sure page
    recycling occurs.

    Close #5021