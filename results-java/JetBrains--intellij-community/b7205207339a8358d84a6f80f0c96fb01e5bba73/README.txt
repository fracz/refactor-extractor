commit b7205207339a8358d84a6f80f0c96fb01e5bba73
Author: Dmitry Batrak <Dmitry.Batrak@jetbrains.com>
Date:   Fri Nov 18 16:15:20 2016 +0300

    hide cursor only on explicit editing actions (typing, enter, backspace, tab) (IDEA-93637)

    this won't trigger now on refactorings and other potentially long operations, so responsiveness should be better (mouse will appear quicker when user moves the mouse)