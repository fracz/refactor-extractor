commit 781b1e34813b20168fde8e7ecc0b1da17496e8c9
Author: Jérôme <jerome.vasseur@jhome.fr>
Date:   Tue Jun 20 08:50:34 2017 +0200

    Only add classes to compile on PHP < 7.0 (#4520)

    addClassesToCompile is deprecated and doesn't improve performances on PHP > 7.0