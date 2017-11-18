commit e7ae2505405cf30d9d3140278d5b9a2604d866df
Author: Craig Mautner <cmautner@google.com>
Date:   Mon Mar 26 17:11:19 2012 -0700

    Move wallpaper animations int WindowAnimator.

    More refactoring. This time wallpaper animations were broken up from
    WindowManagerService and the layout piece kept there while the
    animation piece was moved into WindwoAnimator.

    Also, applyAnimationLocked and applyEnterAnimationLocked were moved
    from WindowManagerService to WindowState.

    Change-Id: I05935023702ce05fdfdc804342ec14f719cdfea4