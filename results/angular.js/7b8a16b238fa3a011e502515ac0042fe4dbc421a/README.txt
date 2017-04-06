commit 7b8a16b238fa3a011e502515ac0042fe4dbc421a
Author: Lucas Galfaso <lgalfaso@gmail.com>
Date:   Wed Jul 29 17:06:35 2015 +0100

    refactor($locale): use en-us as generic built-in locale

    Previously there was a custom built en-us locale that was included with
    angular.js. This made likely that it would get out of sync with the real
    en-us locale that is generated from the closure library.

    This change removes that custom one and uses the generated one instead.
    This also has the benefit of preventing the unwanted caught error on trying
    to load `ngLocale` during angular bootstrap.

    Closes #12462
    Closes #12444
    Closes #12134
    Closes #8174