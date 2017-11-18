commit aa258af65e55c15ce2ba9a036b51176f76c40ba1
Author: Massimo Carli <maxcarli@fb.com>
Date:   Thu Feb 25 08:17:50 2016 -0800

    Created AnimatedImageFactory abstraction - The BIG one

    Summary:In this diff we want to abstract the AnimatedImageFactory from its optional implementation. This is the main diff of the series and basically creates interfaces into a module which could be implemented by implementation in the optional modules. In the specific:

    imagepipeline-base
    -----------------------
    Contains all the interfaces for abstractions
    AnimatedDrawableFactory
    AnimatedFactory (new interface to abstract all the animated*** classes)
    AnimatedImageFactory

    AnimatedFactory is the interface which will be implemented into the animated-base module if some animations are present. The animated-gif and animated-webp have animated-base as dependency so adding one of them will add animated-base as well.
    AnimatedFactoryProvider is the class responsible to check for the presence of AnimatedFactory implementation.

    animation-base
    -----------------------
    This modules contains the implementation for the AnimatedFactory which basically is responsible to check if implementation of the AnimatedImageDecoder interface are present for Gif and Webp. The AnimatedImageDecoder is the interface which abstract the decoding of animated image for Gif and Webp.
    The

    animated-webp
    ------------------
    This contains the implementation of the AnimatedImageDecoder interface for Webp Images. This is an optional module the presence is checked into the animated-base module

    animated-gif
    ------------------
    This contains the implementation of the AnimatedImageDecoder interface for Gif Images. This is an optional module the presence is checked into the animated-base module

    Unfortunately It's difficult to split this into other diffs.
    This builds also on gradle

    UPDATE: Talking to tyronen, we agreed we can reduce dependencies with animation abstracting more the AnimatedDrawable objects. I will be doing this into a new diff [11bis/12].

     ImagePipelineFactory refactoring for optional animation support

    Depends on D2878272

    Reviewed By: kirwan

    Differential Revision: D2881173

    fb-gh-sync-id: 7b5ce9dc65f883c1db01bfbace121154eeb9e52e
    fbshipit-source-id: 7b5ce9dc65f883c1db01bfbace121154eeb9e52e