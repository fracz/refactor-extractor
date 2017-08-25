commit e11a38765d25b2207aea1edb6bd3da8dd922db1f
Author: Ondrej Mirtes <ondrej@mirtes.cz>
Date:   Sun Mar 26 21:48:00 2017 +0200

    Nullables as unions and lot of other improvements!

    * Nullables are now part of UnionType. [BC break - Type::isNullable removed)
    * As a result, functions/methods can check for returned nullables better than before
    * Better support for types in ternary operator
    * Eliminating null from types if detected in if/elseif condition
    * Properties and static properties are not typed to null when they are checked against null