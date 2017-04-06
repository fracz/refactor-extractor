commit 3bbb6e599316b2a46ca521f94fa5e9d9dab09a85
Merge: 4b476c3 73312ab
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Oct 15 03:12:16 2011 +0200

    merged branch hhamon/type_validator_fix (PR #2379)

    Commits
    -------

    73312ab [Validator] The Type constraint now accepts the "Boolean" type instead of "boolean".

    Discussion
    ----------

    [Validator] The Type constraint now accepts the "Boolean" type instead of

    [Validator] The Type constraint now accepts the "Boolean" type instead of "boolean".

    Bug fix: yes
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets: -

    ---------------------------------------------------------------------------

    by stloyd at 2011/10/11 03:43:24 -0700

    As this is bugfix only, this should be made againts __2.0__ branch.

    ---------------------------------------------------------------------------

    by hhamon at 2011/10/11 05:03:52 -0700

    @stloyd I don't really know if it's a bug fix or an improvement. We could not use "Boolean" but we could use "boolean" or "bool" only. So that's why I just improved this constraint validator.