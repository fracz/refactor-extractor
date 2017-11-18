commit c71c344b8a46428174db59050f08a19a1fa3fd93
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Mon Feb 22 16:53:55 2016 +0300

    Fix NCDFE on primitive iterators during boxing optimization

    The real fix is in ProgressionIteratorBasicValue's constructor, other changes
    are refactorings

     #KT-11153 Fixed