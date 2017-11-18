commit b185fd76845a9c99e4077bc017d25d65ed58d440
Author: xiaojian cai <mc02cxj@gmail.com>
Date:   Sun Oct 8 06:23:00 2017 +0800

    patch about issue #836 addChildren is quadratic (#930)

    * patch about issue #836 addChildren is quadratic

    patch about  issue #836 addChildren is quadratic
    when add many children, the origin implementation will move childNodes array per loop.(method add(index, obj)),
    a performance improvement was made using addAll(index, objs).there are three steps:
    1. reparent all the children
    2. use addAll(index, objs) to insert children
    3. reset sibling index
    it is very similar with method addChildren(Node... children)