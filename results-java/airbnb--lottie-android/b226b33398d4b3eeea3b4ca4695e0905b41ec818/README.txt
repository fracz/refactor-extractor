commit b226b33398d4b3eeea3b4ca4695e0905b41ec818
Author: Gabriel Peal <gpeal@users.noreply.github.com>
Date:   Tue Sep 19 11:33:51 2017 -0700

    Lots of scaling improvements (#461)

    Lottie should now respect scaling and scale types much better. Things like centerInside and centerCrop will now work as expected. The only limitation is that if you set the scale to larger than the canvas then it will only scale up to the maximum size for the current scale type (centerInside/centerCrop). If you change the canvas scaling from extraScale * extraScale then it will scale larger but it'll also end up scaling when you have a fixed width which you don't actually want. For now, I'm optimizing for the case where you want the animation to fit the view.