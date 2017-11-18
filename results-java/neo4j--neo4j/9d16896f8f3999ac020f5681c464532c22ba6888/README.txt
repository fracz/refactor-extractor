commit 9d16896f8f3999ac020f5681c464532c22ba6888
Author: Pontus Melke <pontusmelke@gmail.com>
Date:   Mon Aug 28 11:11:58 2017 +0200

    More efficient parameter handling in compiled runtime

    After the big value refactoring the compiled runtime had to do a lot
    of unnecessary work on parameters, going from objects to values, back to
    objects and then also introspect all those objects. This changes so that
    it no longer needs to do that work.