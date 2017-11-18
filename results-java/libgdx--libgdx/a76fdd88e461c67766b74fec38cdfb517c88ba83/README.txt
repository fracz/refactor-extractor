commit a76fdd88e461c67766b74fec38cdfb517c88ba83
Author: badlogicgames <badlogicgames@6c4fd544-2939-11df-bb46-9574ba5d0bfa>
Date:   Sun Oct 17 17:12:45 2010 +0000

    [changed] AndroidApplication.initialize() refactored, doesn't take a LayoutParams anymore. Will just set the GLSurfaceView to the contentView, filling it.
    [added] AndroidApplication.initializeForView(), will return the GLSurfaceView (but typed to a View, don't mess with it) so you can put it in your layout wherever you want.