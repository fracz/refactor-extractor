commit 283fe70f0be6c36619551522ab26ef8ce392fb0f
Author: Eduardo Macarron <eduardo.macarron@gmail.com>
Date:   Sun Jan 30 18:15:23 2011 +0000

    Fix for http://code.google.com/p/mybatis/issues/detail?id=197  and hopefully http://code.google.com/p/mybatis/issues/detail?id=147 . Ignore resetAutocommit exceptions and refactored exception handling on close.