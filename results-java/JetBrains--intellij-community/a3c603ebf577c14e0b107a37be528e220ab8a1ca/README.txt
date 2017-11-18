commit a3c603ebf577c14e0b107a37be528e220ab8a1ca
Author: Eugene Zhuravlev <jeka@intellij.com>
Date:   Sun Apr 22 22:12:44 2012 +0200

    - refactor:handle cache corruptions outside IncProjectBuilder
    - when forcing rebuild because of cache corruption, keep the original compilation scope