commit a59ebb7e5316297d67a1a8e2c8cc90e987c9838c
Author: Felix Feisst <feisst.felix@gmail.com>
Date:   Sun May 22 13:01:01 2016 +0200

    HHH-10762 -Implemented left joins for relation traversion in audit
    queries by leveraging the new HQL feature to join unrelated entities.
    Furthermore, the implementation of inner joins have been improved by
    using the same new HQL feature. The audit query API has been extended to
    support criterias where two properties of different entities are
    disjuncted, conjuncted or directly compared to each other.