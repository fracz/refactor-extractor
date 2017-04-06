commit 97b3e003bc60f979fddecd16bc50304b5bef7e53
Author: Martin Staffa <mjstaffa@googlemail.com>
Date:   Sat Apr 9 11:06:02 2016 +0200

    perf(ngOptions): use documentFragment to populate select

    This changes the way option elements are generated when the ngOption collection changes.
    Previously, we would re-use option elements when possible (updating their text and
    label). Now, we first remove all currently displayed options and the create new options for the
    collection. The new options are first appended to a documentFragment, which is in the end appended
    to the selectElement.

    Using documentFragment improves render performance in IE with large option collections
    (> 100 elements) considerably.

    Creating new options from scratch fixes issues in IE where the select would become unresponsive
    to user input.

    Fixes #13607
    Fixes #13239
    Fixes #12076