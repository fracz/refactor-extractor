commit 70ce425e6a5c9d18caab754d03f6494b5174774e
Author: Peter Bacon Darwin <pete@bacondarwin.com>
Date:   Thu Jul 16 18:28:55 2015 +0100

    refactor($locale): use en-us as generic built-in locale

    Previously there was a custom built en-us locale that was included with
    angular.js. This made likely that it would get out of sync with the real
    en-us locale that is generated from the closure library.

    This change removes that custom one and uses the generated one instead.
    This also has the benefit of preventing the unwanted caught error on trying
    to load `ngLocale` during angular bootstrap.

    Closes #12134
    Closes #8174