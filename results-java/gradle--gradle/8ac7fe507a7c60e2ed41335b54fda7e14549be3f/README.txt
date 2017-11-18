commit 8ac7fe507a7c60e2ed41335b54fda7e14549be3f
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Wed Nov 13 18:55:03 2013 +0100

    Improved status/progress bar in few ways.

    1. New info in the status bar:
     - added % to the build progress (tasks done/all tasks)
     - changed 'Loading' to 'Configuring'
     - log number of projects/total projects configured (cannot use the % here because we don't always know how many projects need to be configured upfront - configuration on demand, etc.)
     - log the project that is being configured

    Examples:

    > Configuring > 1/25 projects > :project1
    > Building 25% > :core:codenarcTest

    2. Some refactoring and rework is pending. The parallel status bar looks worse with this change but it will be fixed with the next round of commits.