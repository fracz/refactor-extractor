commit c677675e9c465dc1de21ecf2e0421835c7eb55b4
Author: Florin Malita <fmalita@google.com>
Date:   Tue May 6 21:07:19 2014 -0400

    Encapsulate Canvas.mNativeCanvas

    Currently, the native canvas is accessed/manipulated from several
    unrelated classes.

    In order to facilitate SaveFlags emulation, this CL encapsulates
    the field and refactors its external users.

    Two main changes:

    * new getNativeCanvas() getter for use in Java-level clients.
    * JNI canvas swappers (GraphicsBuffers, Surface, TextureView &
      AssetAtlasService) are refactored based on the exising/equivalent
      safeCanvasSwap() Canvas method.

    Change-Id: I966bd4898f0838fb3699e226d3d3d51e0224ea97