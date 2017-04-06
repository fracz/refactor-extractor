commit 25a62b58db31c212c330d1bd7ce58bdd031e114a
Author: Misko Hevery <misko@hevery.com>
Date:   Tue Aug 2 13:29:12 2011 -0700

    refactor(injection) infer injection args in ng:controller only

    Because only controllers don't have currying, we can infer its arguments, all other APIs needing currying, automatic inference complicates the matters unecessary.