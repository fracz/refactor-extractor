commit 4175860af1ea51ddc525a73f586506fa14f35816
Author: Martin Staffa <mjstaffa@googlemail.com>
Date:   Tue Sep 1 19:05:04 2015 +0200

    docs(ngModel): improve the `$setViewValue` documentation

    - reorder the paragraphs to highlight more important info
    - clarify what can / should be passed to the method,
    and what to (not) expect from it
    - clarify when the method will trigger a digest

    Closes #12713
    Closes #11121
    Closes #12498