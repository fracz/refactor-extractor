commit 91ad79fb49bc9a2b5b247105acaafdf27482d1a0
Author: nathan.sweet <nathan.sweet@6c4fd544-2939-11df-bb46-9574ba5d0bfa>
Date:   Mon Oct 17 00:02:25 2011 +0000

    Scene2d refactoring + more.
    [added] WidgetGroup, a Group that does Layout.
    [updated] Layout, added pack, invalidateHierarchy, and validate.
    [fixed] Group lastTouchedChild.
    [fixed] Stack layout.
    [updated] LwjglFrame, setVisible called automatically.
    [fixed] LwjglCanvas cursor.
    [fixed] Warnigns, javadocs.
    [fixed] TextureRegion size float rounding.
    [updated] TableLayout pref size calculation.
    [fixed] TextField cursor blink when clicked.