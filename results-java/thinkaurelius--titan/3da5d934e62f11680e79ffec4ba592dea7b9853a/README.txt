commit 3da5d934e62f11680e79ffec4ba592dea7b9853a
Author: Matthias Broecheler <me@matthiasb.com>
Date:   Sat Dec 7 17:45:19 2013 -0800

    Changed the shading to exclude all "org" dependencies which required some refactoring in Faunus. Included shaded dependencies on titan-cassandra and titan-hbase. Refactored FaunusTitanGraph to abstract all titan version specific elements in the respective delegated class. Some debugging.