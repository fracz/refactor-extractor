commit 88ce26f8ca405a7572ef584b63131e0ef3c4b170
Author: John Sterling <hruntio@gmail.com>
Date:   Sat Jun 18 23:03:20 2016 -0400

    Improve performance of File\X509->_mapInExtensions() for large arrays

    This avoids passing array references by-value to is_array()
    (which would trigger a copy) by refactoring _subArray() into
    a separate is_array() check on a by-value var, and a separate
    unchecked reference return.