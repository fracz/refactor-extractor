commit bf1fcc22b42397d6573a0c99ca86e8c964617f4b
Author: gwoo <gwoohoo@gmail.com>
Date:   Fri Jun 4 16:38:16 2010 -0700

    refactored handling of content type. Moved type() method into `\net\http\Message`.
    `\net\http\Message` now has a dependency on `\net\http\Media` to return the alias for the content type.