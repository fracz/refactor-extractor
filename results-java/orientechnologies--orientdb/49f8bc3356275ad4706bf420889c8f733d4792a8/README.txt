commit 49f8bc3356275ad4706bf420889c8f733d4792a8
Author: lvca <l.garulli@gmail.com>
Date:   Fri Mar 25 04:41:49 2016 +0100

    Index: improved performance for most common use cases + reduced code

    Avoided array creation+copy+sort for the most common use case: 1 key
    (non composite index)