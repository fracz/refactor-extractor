commit caf11ff2fbd9195cc780edb375f6a74302d906df
Author: Boaz Leskes <b.leskes@gmail.com>
Date:   Tue Jul 8 13:34:24 2014 +0200

    Revert "[Discovery] immediately start Master|Node fault detection pinging"

    In #6706 we change the master validation to start pining immediately after a new master as ellected or a node joined. The idea is to have a quicker response to failures. This does however create a problem if the new master has yet fully processed it's ellection and responds to the ping with a NoLongerMasterException. This causes the source node to remove the current master and ellect another, only to find out it's not a master either and so forth. We are moving this change to the feature/improve_zen branch, where the improvements we made will cause the situation to be handled properly.

    This reverts commit ae16956e072bea317ea481f65f2e110dc48fde17.