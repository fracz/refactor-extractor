commit 450a2b358dde00f4d89c0f6a63f8f552fdf7ae0a
Author: Jeroen Dalsem <jdalsem@coldtrick.com>
Date:   Wed Apr 29 17:10:29 2015 +0200

    chore(views): refactored widget content view

    Both the action and the object/widget view did the same content related
    checks. With this refactor the both use the same
    object/widget/elements/content view to handle the logic for the content
    output (no view logic in the actions). Reducing boilerplate.

    Improved readability of the object/widget view

    A side effect is that it now is easier to modify the generic content
    output of a widget instead of the full widget view. Now it is also
    easier to extend.