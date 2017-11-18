commit 8b081ca8a916619c4f9d43950ea1cb3a8dee4830
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Wed Oct 26 20:35:34 2011 +0400

    Git branch refactoring

    * Extract GitNewBranchNameValidator to a new class; extract showing dialog to enter new branch name to GitBranchUiUtil; made GitBranchPopup package again, reverted extensions on NewBranchAction.
    * GitLogUI: Don't call GitBranchPopup.NewBranchAction, call GitBranchOperationsProcessor directly.
    * GitBranchOperationsProcessor rename checkoutNewTrackingBranch to checkoutNewBranchStartingFrom and all parameters-variables and javadocs, since it is more general name, and Git settles tracking automatically if the reference given is a remote branch. Same in Git.checkout.
    * Remove 'checking out from some start point' functionality from createNewBranch since it goes to cehckoutNewBranchStartingFrom.