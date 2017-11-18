commit 70efea2e0ac59d22e5ebb4192757741453d8ea8e
Author: Gabriel Peal <gpeal@users.noreply.github.com>
Date:   Thu Mar 9 23:22:42 2017 -0800

    Ground up rendering engine refactor (#184)

    This is a ground up refactor of the rendering engine.
    At a high level:

    Layers now map much more closely to the way they are structured in After Effects.
    ShapeLayer, PrecompLayer, NullLayer, SolidLayer, and ImageLayer are now individual subclasses of BaseLayer.
    Everything is now a drawing and or path content and has the same layer structure as appears in After Effects.
    Drawing contents are fill and stroke not shape like they used to be. This allows features like fill type and merge paths.
    Cumulative parent matrices are now sent all the way down to the shape instead of scaling the canvas. This allows a single transform to be done on the shape itself. It may also improve the ability to use hardware acceleration because shapes will be properly scaled up rather than being on a canvas that's scaled.