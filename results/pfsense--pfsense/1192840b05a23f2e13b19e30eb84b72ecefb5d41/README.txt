commit 1192840b05a23f2e13b19e30eb84b72ecefb5d41
Author: Sjon Hortensius <sjon@hortensius.net>
Date:   Sat Mar 28 15:33:23 2015 +0100

    refactor attributes from Input to Element to make it a bit more generic

    * replaced Input::setAttribute with explicit setReadonly/setDisabled
    * introduced Input::toggles

    fixes #37, refs #21