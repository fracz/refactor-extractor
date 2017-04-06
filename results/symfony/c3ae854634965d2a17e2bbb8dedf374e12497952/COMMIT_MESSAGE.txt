commit c3ae854634965d2a17e2bbb8dedf374e12497952
Merge: 7f9fd11 1fa22d9
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Jul 9 16:11:12 2012 +0200

    merged branch SamsonIT/error_explaining_lack_of_adder_remover (PR #4777)

    Commits
    -------

    1fa22d9 [Form] Output a more usable error when PropertyPath has tried to find adders and getters, but failed to find them

    Discussion
    ----------

    [Form] Output a more usable error when PropertyPath has tried to find ad...

    ...ders and getters, but failed to find them

    Bug fix: no
    Feature addition: yes
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets: -

    I've refactored the writeProperty method of propertypath in order to supply a better error message when writing has failed.

    The writeProperty method itself now finds singulars (if a singular was not passed) for the private findAdderAndRemover method which allowed for some duplicate code to be removed and since the writeProperty now holds this data, it can provide a more verbose exception message.

    ---------------------------------------------------------------------------

    by bschussek at 2012-07-09T13:54:35Z

    Apart from the typo this PR looks good.

    ---------------------------------------------------------------------------

    by Burgov at 2012-07-09T14:01:04Z

    fixed&squashed