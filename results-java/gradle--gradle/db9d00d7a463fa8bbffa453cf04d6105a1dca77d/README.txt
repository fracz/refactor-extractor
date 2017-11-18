commit db9d00d7a463fa8bbffa453cf04d6105a1dca77d
Author: Rodrigo B. de Oliveira <rodrigo@gradle.com>
Date:   Thu Jan 21 11:53:02 2016 -0200

    `coll.toArray(new T[coll.size()])` => `coll.toArray(new T[0])`

    > Bottom line: toArray(new T[0]) seems faster, safer, and contractually cleaner, and therefore should be the default choice now. Future VM optimizations may close this performance gap for toArray(new T[size]), rendering the current "believed to be optimal" usages on par with an actually optimal one. Further improvements in toArray APIs would follow the same logic as toArray(new T[0]) — the collection itself should create the appropriate storage.

    http://shipilev.net/blog/2016/arrays-wisdom-ancients/

    Let's see what our performance tests say, if this is noticeable at all.

    Changes made by IDEA "Structural Replace":

       $coll$.toArray(new $T$[$coll$.size()])

    =>

       $coll$.toArray(new $T$[0])

    +review REVIEW