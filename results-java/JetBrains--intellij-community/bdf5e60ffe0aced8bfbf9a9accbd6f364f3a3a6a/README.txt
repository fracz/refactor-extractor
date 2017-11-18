commit bdf5e60ffe0aced8bfbf9a9accbd6f364f3a3a6a
Author: Julia Beliaeva <Julia.Beliaeva@jetbrains.com>
Date:   Wed Aug 30 21:12:53 2017 +0300

    Revert "[vcs-log] refactor hardcoded colors to color-keys"

    This reverts commit a0a39a154d0d05f7428d79759bd029b5bbb36fb7.

    Vcs Log colors should not be tied to editor color scheme since it is not in sync with the ui theme. So another implementation is required here, similar to "File Status Colors". Also better ui for color settings should be developed, since the standard one has also text style settings that are not applicable for reference colors.