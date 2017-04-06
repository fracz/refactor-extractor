commit d8be36cf849341519b34a27d1426d648eea60072
Merge: 64bf80c b1f190f
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Jan 7 08:07:44 2013 +0100

    merged branch ricardclau/improve-luhn-validator (PR #6591)

    This PR was merged into the master branch.

    Commits
    -------

    b1f190f covers edge case for Luhn

    Discussion
    ----------

    [Validator] Covering edge case for Luhn when checksum is exactly 0

    Bug fix: yes
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets:
    Todo:
    License of the code: MIT
    Documentation PR:

    Actually not a big deal, but a Credit Card with all 0s passed validation. Now this is covered.