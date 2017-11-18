commit 4238e3e4b5edbd7e28d0d929ac0c4fdbecd7b100
Author: Craig Mautner <cmautner@google.com>
Date:   Thu Mar 28 15:28:55 2013 -0700

    Make the min layer go down through all windows

    The min layer was set to only show the windows that matched
    the specified app token. But that meant when dialogs were
    launched it only showed the dialogs and not the background
    windows.

    Added improved debugging.

    fixes bug 8502844.

    Change-Id: I26b49568b872801ec9aa088df20317aa752dacd6