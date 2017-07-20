commit f47e0933c3263abc7840c59bfa04bbbde6e730c2
Author: Michal Čihař <mcihar@novell.com>
Date:   Thu Sep 16 15:19:46 2010 +0200

    [core] Force generating of new session on login

    This improves security because session ID and token are generated fresh
    for each user.