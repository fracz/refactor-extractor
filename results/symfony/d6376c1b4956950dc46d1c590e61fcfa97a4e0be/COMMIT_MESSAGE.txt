commit d6376c1b4956950dc46d1c590e61fcfa97a4e0be
Merge: f73bced 1290b80
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Apr 19 16:34:09 2013 +0200

    merged branch bschussek/issue5899 (PR #6573)

    This PR was merged into the master branch.

    Discussion
    ----------

    [2.3] [Form] Renamed option "virtual" to "inherit_data" and improved handling of such forms

    Bug fix: yes
    Feature addition: yes
    Backwards compatibility break: yes
    Symfony2 tests pass: yes
    Fixes the following tickets: #5899, #5720, #5578
    Todo: -
    License of the code: MIT
    Documentation PR: symfony/symfony-docs#2107

    This PR renames the option "virtual" to "inherit_data" for more clarity (the old option is deprecated and usable until 2.3). It also fixes the behavior of forms having that option set.

    Forms with that option set will now correctly return their parents' data from `getData()`, `getNormData()` and `getViewData()`. Furthermore, `getPropertyPath()` was fixed for forms that inherit their parent data.

    Commits
    -------

    1290b80 [Form] Fixed the deprecation notes for the "virtual" option
    ac2ca44 [Form] Moved parent data inheritance from data mappers to Form
    8ea5e1a [Form] Renamed option "virtual" to "inherit_data"