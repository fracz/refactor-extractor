commit 40b3f33f8d8151a6d230902b4e204fae4df79aa4
Author: Irina.Chernushina <Irina.Chernushina@jetbrains.com>
Date:   Tue Apr 25 18:01:48 2017 +0200

    json schema refactoring:
    - correct resolve for navigation (do not expand the last step)
    - cache expanded schema on its psi element