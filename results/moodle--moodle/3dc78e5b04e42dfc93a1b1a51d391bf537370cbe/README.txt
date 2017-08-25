commit 3dc78e5b04e42dfc93a1b1a51d391bf537370cbe
Author: David Mudrak <david.mudrak@gmail.com>
Date:   Mon Jan 4 18:02:05 2010 +0000

    Refactoring some critical parts

    Critical issue fixed here: after recent refactorings, I called
    get_users_with_capability() inside the loop in another loop! (oops :-)
    This was used in my very first code using the renderers. I have learnt
    I should follow the core approach (as suggested by Tim) to prepare a
    data object and pass it to the renderer. The fact the renderer called
    a workshop method indicated something was really wrong...

    WIP