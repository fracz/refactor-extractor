commit 73c038fb48d11021029ec86d74b0958897a41b8b
Author: Martijn van Groningen <martijn.v.groningen@gmail.com>
Date:   Wed Aug 7 13:20:21 2013 +0200

    Improved filtering by _parent field

    In the _parent field the type and id of the parent are stored as type#id, because of this a term filter on the _parent field with the parent id is always resolved to a terms filter with a type / id combination for each type in the mapping.

    This can be improved by automatically use the most optimized filter (either term or terms) based on the number of parent types in the mapping.

    Also added support to use the parent type in the term filter for the _parent field. Like this:
    ```json
    {
       "term" : {
            "_parent" : "parent_type#1"
        }
    }
    ```
    This will then always automatically use the term filter.

    Closes #3454