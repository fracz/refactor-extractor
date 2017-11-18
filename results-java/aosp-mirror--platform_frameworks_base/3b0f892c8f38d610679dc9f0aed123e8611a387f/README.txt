commit 3b0f892c8f38d610679dc9f0aed123e8611a387f
Author: Muyuan Li <muyuanli@google.com>
Date:   Mon Apr 18 19:27:29 2016 -0700

    sysui: refactor for extensibility.

    1. Refactor the interfaces of the slider out side the ToggleSlider class.
    2. BrightnessController now takes in only an IToggleSlider interface instead of
       the view.

    Bug: 28172423
    Change-Id: Iff463a30e149f17795ccaffca66040d64f44a643
    (cherry picked from commit 89c0cb853175c66769d287b8d6cf82c7166e0e6e)