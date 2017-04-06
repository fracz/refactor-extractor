commit 7ae6b06822caa8de00e3433d281d7f60600b3ad9
Merge: de958c7 04cb5bc
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Jul 28 12:15:36 2012 +0200

    merged branch bschussek/optimize-config (PR #5090)

    Commits
    -------

    04cb5bc [Form] Removed the ImmutableFormConfig class to save time spent with copying values (+20ms)

    Discussion
    ----------

    [Form] Removed the ImmutableFormConfig class to improve performance

    Bug fix: no
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets: -
    Todo: -

    Performance gain: 20ms