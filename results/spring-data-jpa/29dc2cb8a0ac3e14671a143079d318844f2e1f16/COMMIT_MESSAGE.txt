commit 29dc2cb8a0ac3e14671a143079d318844f2e1f16
Author: Oliver Gierke <ogierke@gopivotal.com>
Date:   Mon Jun 3 13:40:47 2013 +0200

    DATAJPA-350 - Improve EntityManager usage for query validation.

    SimpleJpaQuery now uses an explicitly created EntityManager instance to verify the manually defined query be able to close the instance explicitly. This will improve GCing the instance.