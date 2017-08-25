commit 974053b93f9d64ff45aa0a3d02de07b42bd6139f
Author: David Monllao <davidm@moodle.com>
Date:   Wed Sep 16 13:32:29 2015 +0800

    MDL-48881 mod_lesson: A couple of performance improvements

    - Improve memory usage on db drivers not able to use recordsets
      properly
    - !array_key_exists replaced by empty calls