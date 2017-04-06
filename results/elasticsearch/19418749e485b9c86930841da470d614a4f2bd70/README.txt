commit 19418749e485b9c86930841da470d614a4f2bd70
Author: javanna <cavannaluca@gmail.com>
Date:   Wed Sep 3 12:31:16 2014 +0200

    Java api: change base class for GetIndexedScriptRequest and improve its javadocs

    `GetIndexedScriptRequest` now extends `ActionRequest` instead of `SingleShardOperationRequest`, as the index field that was provided with the previous base class is not needed (hardcoded).

    Closes #7553