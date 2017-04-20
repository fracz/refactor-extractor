commit ad04efe5cc8897db67f3f541e7705193caacc2ae
Author: Intiilapa <intiilapa-zf@ssji.net>
Date:   Fri Nov 18 19:53:07 2011 +0100

    Logger refactor: add Iterator support for extra

    Update signature of Logger:log() method to accept Traversable as extra information. All iterators are converted to an array (like an instance of ArrayObject). ZF2 is oriented SOLID paradigm: containers are often an object.