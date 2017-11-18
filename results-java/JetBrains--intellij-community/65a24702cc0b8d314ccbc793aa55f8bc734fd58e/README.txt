commit 65a24702cc0b8d314ccbc793aa55f8bc734fd58e
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Tue Nov 8 20:47:36 2011 +0300

    Git push result notification refactoring

    Don't pass pushInfo to the results notifications, since the pushInfo change between consecutive pushes.
    Instead store commit number (for now we need only this to display the result) in GitPushBranchResult.