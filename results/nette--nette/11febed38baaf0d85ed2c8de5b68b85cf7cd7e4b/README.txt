commit 11febed38baaf0d85ed2c8de5b68b85cf7cd7e4b
Author: David Grudl <david@grudl.com>
Date:   Fri Jul 11 02:21:02 2008 +0000

    - important: renamed Control::invalidate() -> invalidatePartial(), validate() -> validatePartial(), isInvalid() -> isPartialInvalid()
    - template handling moved from Presenter -> Control
    - fixed url encoding/decoding in Route (and some refactoring done)
    - improved syntax highlighting in Debug
    - Debug bluescreen page is encoded in UTF-8 now
    - improved Uri::unescape()