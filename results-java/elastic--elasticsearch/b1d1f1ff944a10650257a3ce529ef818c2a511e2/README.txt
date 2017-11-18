commit b1d1f1ff944a10650257a3ce529ef818c2a511e2
Author: kimchy <kimchy@gmail.com>
Date:   Thu Aug 12 12:31:27 2010 +0300

    improve peer recovery shard state handling. when throttling a recovery, don't restore the non recovering state and then move to recovery again with each retry, keep the recovering state while retrying