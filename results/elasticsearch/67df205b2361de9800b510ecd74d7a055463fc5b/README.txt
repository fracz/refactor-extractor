commit 67df205b2361de9800b510ecd74d7a055463fc5b
Author: Boaz Leskes <b.leskes@gmail.com>
Date:   Sat Mar 7 15:41:50 2015 +0100

    Gateway: improve assertion at the end of shard recovery

    we want to make sure the recovery finished all the way to post recovery. Current check, validating the shard is either in POST_RECOVERY or STARTED is not good because the shard could be also closed if things go fast enough (like in our tests). The assertion is changed to check the shard is not left in CREATED or RECOVERING.

    Closes #10028