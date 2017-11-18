commit 8ce00301a023eecaeb8891ce906f67b513ebb42a
Author: Romain Guy <romainguy@google.com>
Date:   Tue Jan 15 18:51:42 2013 -0800

    Implement clipRect with a transform, clipRegion & clipPath
    Bug #7146141

    When non-rectangular clipping occurs in a layer the render buffer
    used as the stencil buffer is not cached. If this happens on a
    View's hardware layer the render buffer will live for as long
    as the layer is bound to the view. When a stencil buffer is
    required because of a call to Canvas.saveLayer() it will be allocated
    on every frame. A future change will address this problem.

    If "show GPU overdraw" is enabled, non-rectangular clips are not
    supported anymore and we fall back to rectangular clips instead.
    This is a limitation imposed by OpenGL ES that cannot be worked
    around at this time.

    This change also improves the Matrix4 implementation to easily
    detect when a rect remains a rect after transform.

    Change-Id: I0e69fb901792d38bc0c4ca1bf9fdb02d7db415b9