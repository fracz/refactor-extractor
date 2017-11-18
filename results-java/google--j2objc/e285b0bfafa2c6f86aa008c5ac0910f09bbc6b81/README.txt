commit e285b0bfafa2c6f86aa008c5ac0910f09bbc6b81
Author: zgao <zgao@google.com>
Date:   Fri Jan 27 07:56:21 2017 -0800

    Cache method selectors

    Method NameTable.getOriginalMethod() is one of the top methods in the translator's profile. This CL has 7-8% improvement on our Guava benchmark.

            Change on 2017/01/27 by zgao <zgao@google.com>

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=145795279