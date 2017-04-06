commit be3b62cd09e770861bf446edc0ed5932ba50f160
Author: Peter Bacon Darwin <pete@bacondarwin.com>
Date:   Mon Sep 8 10:14:17 2014 +0100

    chore(bower): refactor bower usage

    The gulp bower task in the docs app was never actually running since it couldn't
    find the bower.json file and was silently failing. Updating to a newer bower
    highlighted this issue.

    This commit moves the docs app specific bower components into the docs folder.
    There are only jquery and closure compiler related components in the project
    folder now.

    It also improves the gulp bower task to provide better feedback of progress
    and errors.