commit 845f4f2bb7791f4c759e06b7f22109e33ba91b5d
Author: Sam Brannen <sam@sambrannen.com>
Date:   Sun May 31 15:37:31 2015 +0200

    Improve attribute alias support in @ComponentScan

    Prior to this commit, @ComponentScan already had a value/basePackages
    alias pair; however, the semantics were not properly enforced.

    This commit addresses this issue by refactoring
    ComponentScanAnnotationParser to ensure that it is not possible to
    declare both of the aliased attributes. In addition, the 'value' and
    'basePackages' attributes are now annotated with @AliasFor in order to
    make the semantics clearer.

    Issue: SPR-11393