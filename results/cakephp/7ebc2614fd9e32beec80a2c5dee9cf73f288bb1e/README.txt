commit 7ebc2614fd9e32beec80a2c5dee9cf73f288bb1e
Author: Mark Story <mark@mark-story.com>
Date:   Sat Sep 24 09:51:21 2016 -0400

    Add a simpler response emitter.

    This new ResponseEmitter offers some improved ergonomics. It no longer:

    * Throws an exception when headers have been sent.
    * Truncates content when debug output has been generated in the
      controller.

    It also uses setcookie() which lets us remove the shims we had to apply
    to restore behavior of ext/session.

    Refs #9472