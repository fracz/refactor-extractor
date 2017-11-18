commit 9e05d46e0fc60b858a4ef21a9a26e2065fadbb94
Author: Anthony Chen <ajchen@google.com>
Date:   Fri Apr 7 10:10:21 2017 -0700

    Allow notification shelf to be toggled off.

    Android auto's notifications UX does not have a notification shelf. Add
    a config that hides the shelf.

    Ideally, the shelf should be conditionally created, but it is closely
    coupled through system UI. This change would require a larger refactor,
    so just allow it to be hidden for now.

    Also, update some areas that looked at the height of the notification
    shelf. If it has a visibility of GONE, then don't grab the height.

    Test: booted up phone and Android Auto headunit to verify
    Bug: 33210494
    Change-Id: I66b1eb956838e26bf695da01dab9ba5349ee321e