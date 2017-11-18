commit 0d64bdc673472ee966358320b80cf7dd6ea77467
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Wed Feb 29 23:33:37 2012 +0100

    Reused existing algorithm of splitting quoted and space-delimited arguments from the UI. I'm not sure what's the API visibility of UI module classes so I left the external api untouched. Some more refactorings in JvmOptions are pending.