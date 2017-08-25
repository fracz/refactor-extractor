commit 97533ab272878d039ce565f4d31919889b3a5b90
Author: Iglocska <andras.iklody@gmail.com>
Date:   Tue Feb 2 09:35:14 2016 +0100

    Major speed boost to the correlation

    - it seems that for some reason some conditions in the correlation lookup massacred the performance of the correlation
    - doing that additional filter on a PHP level fixes it for now, but it would be interesting to investigate this further and potentially reuse the findings to improve other queries

    - also fixed an issue with the indexing script failing on some fulltext fields if it has to fall back to regular indeces.