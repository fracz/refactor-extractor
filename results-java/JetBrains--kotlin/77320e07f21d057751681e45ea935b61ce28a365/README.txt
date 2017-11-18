commit 77320e07f21d057751681e45ea935b61ce28a365
Author: develar <develar@gmail.com>
Date:   Thu Jun 28 11:13:48 2012 +0400

    ArrayList improvements:

    1) remove must compare object using Kotlin.equals (the same as in contains)
    2) use ArrayIterator instead of ListIterator (perfomance)
    3) remove duplicated code (refactor)
    4) toArray
    5) fix "add((index, item)" — correct translation to JavaScript "addAt"