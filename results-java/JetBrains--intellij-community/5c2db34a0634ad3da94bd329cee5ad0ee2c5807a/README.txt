commit 5c2db34a0634ad3da94bd329cee5ad0ee2c5807a
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Fri Nov 9 19:57:09 2012 +0400

    Git Push refactoring to solve several usability problems with push.

    Introduce GitOutgoingCommitsCollector to take the task of collecting outgoing commits (to push).
    - Move all collecting methods from GitPusher there.
    - GitPusher receives GitPushSpecs and pushes by them. If the list of outgoing commits is not ready, waits to display the number of pushed commits afterwards.
    - GitPushDialog also doesn't deal with collecting commits in background.

    Rename the text field to push to "Target branch" to make it more obvious.
    - Remove the checkbox: what is written in the target branch, that is the destination.
    - Initially select repositories based on the repo sync setting: if repositories are not synced, select only the current root.
    - GitPushDialog to update the Target branch fields on selecting/deselecting of repositories.
    - More to be done with Target branch support.

    Affected usability problems: IDEA-82920, IDEA-83227, IDEA-93310, IDEA-81620, IDEA-78648, IDEA-56183