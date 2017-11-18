commit ac0e5b0bf49ddf798e7e32be74722e0f71310ec7
Author: Shai Ber <shaiber01@gmail.com>
Date:   Tue Feb 3 11:46:31 2015 +0200

    Performance and Battery saving improvements in GifDrawable

    Do not schedule the render of the next frame until the current frame is
    drawn.
    Advantages:
    1. Pre-api 14 there is no good way to know when the app is going to the
    background and therefore, no way to pause animations from running while
    in background and then we get the rendering task being scheduled over
    and over again when nothing is drawn - with this fix, rendering of next
    frame is dependant on drawing of previous one, so when going to
    background all animations pause since nothing is drawn and they resume
    when the app comes back
    2. It is virtually impossible to manage visibility for every single Gif
    and make sure to pause it when not visible (especially if the gif is not
    in a view, but in something like a span) - when the next frame is
    dependent on drawing the frame before then while the animation is not
    visible, it is paused automatically since it is not drawn
    3. We save CPU time by not rendering frames that will not be displayed.
    When the rendering of next frame is dependant on drawing of previous
    ones we render exactly as many frames as are actually displayed, saving
    CPU