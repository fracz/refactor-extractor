commit 107843de4507b3511006cb9c77b8d0364374385a
Author: Tom Hudson <tomhudson@google.com>
Date:   Mon Sep 8 11:26:26 2014 -0400

    Remove status return from all uirenderer::Renderer functions

    This moves the interface closer to android::Canvas. The only use of
    return values was in the OpenGLRenderer subclass; that is replaced
    with an internal dirty flag: returned from finish(), checked by
    CanvasContext.

    This is part of a series of CLs to refactor the Graphics JNI bindings.

    BUG:15672762
    R=djsollen@google.com,ccraik@google.com

    Change-Id: Ifd533eb8839a254b0d3a5d04fc5a2905afdfc89e