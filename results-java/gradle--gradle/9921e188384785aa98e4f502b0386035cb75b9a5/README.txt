commit 9921e188384785aa98e4f502b0386035cb75b9a5
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sat Apr 21 17:12:32 2012 +0200

    ExecHandle improvements: detaching from the child process now detects if the child did not start or exited prematurely. Tuned the daemon exception message to include some extra information.