commit 81b64ed587817f9f9398dc38d0db2c3fac7b5af7
Author: Adrien Grand <jpountz@gmail.com>
Date:   Fri Apr 21 10:33:02 2017 +0200

    IndicesQueryCache should delegate the scorerSupplier method. (#24209)

    Otherwise the range improvements that we did on range queries would not work.
    This is similar to https://issues.apache.org/jira/browse/LUCENE-7749.