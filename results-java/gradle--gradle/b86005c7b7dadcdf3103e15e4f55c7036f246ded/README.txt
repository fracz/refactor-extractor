commit b86005c7b7dadcdf3103e15e4f55c7036f246ded
Author: Stefan Wolf <wolf@gradle.com>
Date:   Fri Sep 16 10:50:26 2016 +0200

    Revert "Remove duplicated code in JacocoPlugin (#692)"

    We finally decided not to include this change since
    the code was not duplicated completely and the
    change did not improve readability

    This reverts the commits
    - 79c3c699ab6f17f8e011017f5e6ab19457a26bd8
    - 68e9485290cf632aa2e0bb44d92ac72b2529c81b
    - b20352f231d50262f3601889c296f149f40ebbf6

    +review REVIEW-6202