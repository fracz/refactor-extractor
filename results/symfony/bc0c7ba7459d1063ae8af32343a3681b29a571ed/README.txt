commit bc0c7ba7459d1063ae8af32343a3681b29a571ed
Merge: 05842c5 f666836
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Apr 11 18:41:05 2012 +0200

    merged branch Tobion/named-variables (PR #3884)

    Commits
    -------

    f666836 [Routing] simplified regex with named variables

    Discussion
    ----------

    [Routing] simplified regex with named variables

    Test pass: yes
    BC break: no

    Since PHP 5.2.2 subpatterns in regex can be simplified from `?P<name>` to `?<name>` (see http://www.php.net/manual/en/regexp.reference.subpatterns.php).
    This enhances readability.