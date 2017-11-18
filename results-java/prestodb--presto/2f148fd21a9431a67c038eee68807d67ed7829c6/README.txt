commit 2f148fd21a9431a67c038eee68807d67ed7829c6
Author: David Phillips <david@acz.org>
Date:   Sat Jan 28 23:03:19 2017 -0800

    Remove checkType utility methods

    Due to improved generics inference in Java 8, the method is not
    type safe at all, as it allows passing completely unrelated objects.
    A normal cast is safer and provides a reasonable error message.