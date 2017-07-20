commit 916848085adcc6504c9cb1757e8fc9cba3eee468
Author: mattab <matthieu.aubry@gmail.com>
Date:   Mon Jul 1 14:08:36 2013 +1200

    * Enabling Twig 'strict_variables' so that we write best code possible, and learn early if some tpl code is not valid. Fixing few bugs that this uncovered
    * refactoring the percent column label in getPercentVisitColumn()
    * removing indexBeforeMenu and putting directly in parent template
    Refs #4019