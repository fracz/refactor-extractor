commit 9becfc168b612805e24619dd6267c62aad001880
Author: Romain Guy <romainguy@google.com>
Date:   Thu Feb 28 18:13:54 2013 -0800

    Restore the ability to track native Surface changes
    Bug #8230990

    ViewRootImpl needs to know when the native Surface objects changes
    to recreate the EGL surface. A recent refactoring in Surface broke
    the behavior of getGenerationId(). This simply restores the old
    behavior (every change increments the generation ID by 1.)

    Change-Id: Ife1df1ffb2ee7a373b8ebf2431192702ba10f344