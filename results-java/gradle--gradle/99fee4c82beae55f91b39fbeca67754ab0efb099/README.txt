commit 99fee4c82beae55f91b39fbeca67754ab0efb099
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri May 11 17:14:47 2012 +0200

    Started providing external gradle module information in Tooling API.

    For now, only idea model is supported. Some refactorings are pending. It's not all beautiful, especially in the area of ide model. However, we will be tidying up the ide plugins model of dependencies soon.