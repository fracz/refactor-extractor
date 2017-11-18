commit 36db127e4733ea8ef41a96be5b683e1a2e705677
Author: Jorim Jaggi <jjaggi@google.com>
Date:   Tue Mar 28 00:43:31 2017 +0200

    Boost thread priority when holding the WM lock

    Test: Take systrace of open app, make sure thead is boosted while
    doing stuff in WM
    Test: Run WmSlam with and without boosting. Observe an
    improvement.
    Bug: 36631902
    Change-Id: Iadb036f8d12bbf59091466500e82207cf6fa85d5