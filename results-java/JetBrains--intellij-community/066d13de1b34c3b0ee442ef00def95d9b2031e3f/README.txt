commit 066d13de1b34c3b0ee442ef00def95d9b2031e3f
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Thu Sep 8 17:45:42 2011 +0400

    Git branches: "smart checkout", refactorings.

    1. More methods in Git class for wrapping native calls to Git.
        Return GitCommandResult from these methods for better identification on whether the operation succeeded and capturing the error output.
    2. Checkout new branch: detect unmerged files.
    3. Checkout existing branch/ref: detect local changes would be overwritten by checkout, show them in a dialog, propose to make a smart checkout (save via GitChangesSaver, checkout, restore, capture conflicts).