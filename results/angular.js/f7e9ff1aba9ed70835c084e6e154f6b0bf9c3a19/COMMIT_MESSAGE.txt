commit f7e9ff1aba9ed70835c084e6e154f6b0bf9c3a19
Author: Matias Niemelä <matias@yearofmoo.com>
Date:   Mon May 4 16:56:45 2015 -0700

    fix(ngAnimate): ensure that the temporary CSS classes are applied before detection

    Prior to 1.4 the `ng-animate` CSS class was applied before the CSS
    getComputedStyle detection was issued. This was lost in the 1.4
    refactor, however, this patch restores the functionality.

    Closes #11769
    Closes #11804