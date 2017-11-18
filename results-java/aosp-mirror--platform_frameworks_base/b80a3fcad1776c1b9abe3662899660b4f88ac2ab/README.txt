commit b80a3fcad1776c1b9abe3662899660b4f88ac2ab
Author: Svetoslav Ganov <svetoslavganov@google.com>
Date:   Thu Sep 15 20:02:52 2011 -0700

    Revamping of the NumberPicker widget for improved usablility.

    1. Now if the widget is not interacted with shows a smaller selector
       wheel with the increment and decrement arrows at the top and bottom
       respectively.

    2. Tapping an arrow button now animates the widget to the new value. i.e.
       rotates the selector whell to the next value.

    3. Fixed a bug that double tapping on the input shows the IME but then
       after pressing an arrow button the IME is not hidden.

    4. Fixed a bug that was exposed via late changes in the framework or the
       graphics and was manifested of the selector wheel not having fading
       edges.

    bug:5251980
    bug:5383502

    Change-Id: I4a089dc69b07a3b28a514017cddf786cb9f4af16