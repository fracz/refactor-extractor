commit f1ebfd7b4720db419ff6905b9eccec05729bcd0b
Merge: 9943fd1 3ad0794
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Jul 26 20:10:55 2015 +0200

    minor #15172 [DependencyInjection] fixed FrozenParameterBag and improved Parameter… (Tomaz Ahlin)

    This PR was squashed before being merged into the 2.3 branch (closes #15172).

    Discussion
    ----------

    [DependencyInjection] fixed FrozenParameterBag and improved Parameter…

    The ParameterBagInterface was missing some @throws annotations, so the FrozenParameterBag class was a violation of Liskov subtitution principle. Also the ParameterBagInterface was missing the remove method.

    (Optionally the ParameterBagInterface can be later split into two smaller interfaces, because the FrozenParameterBag shouldn't have the add, remove methods in the first place.)

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | yes
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets |
    | License       | MIT
    | Doc PR        |

    I have also fixed removing elements from FrozenParameterBag, as introduced by @satahippy
    https://github.com/symfony/DependencyInjection/pull/8

    Commits
    -------

    3ad0794 [DependencyInjection] fixed FrozenParameterBag and improved Parameter…