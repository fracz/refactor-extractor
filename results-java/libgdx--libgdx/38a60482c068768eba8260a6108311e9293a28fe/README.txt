commit 38a60482c068768eba8260a6108311e9293a28fe
Author: Christoph Aschwanden <contact@noblemaster.com>
Date:   Wed Oct 10 19:08:42 2012 +0900

    IOSSound improved: sound FX are played outside the rendering loop for smoother frame rate (little bit of a hack). Some info output converted to Gdx.app.debug(…) => seemed more suitable.