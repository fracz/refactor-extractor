commit 2aacc2d622893e05eb94b3974d562e681cc3a17f
Author: Matias Niemelä <matias@yearofmoo.com>
Date:   Mon May 4 16:08:30 2015 -0700

    fix(ngAnimate): ensure animations are not attempted on text nodes

    With the large refactor in 1.4.0-rc.0, the detection code failed to
    filter out text nodes from animating. This fix ensures that now properly
    happens.

    Closes #11703