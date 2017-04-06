commit 142cffcf64d2dce93089e9a73257bddf50e96990
Author: Di Peng <pengdi@google.com>
Date:   Fri Aug 5 15:01:58 2011 -0700

    refactor(widgets): remove input[button, submit, reset, image] and button windgets

    These widgets are useless and only trigger extra $updateViews.

    The only reason we had them was to support ng:change on these widgets,
    but since there are no bindings present in these cases it doesn't make
    sense to support ng:change here. It's likely just a leftover from
    getangular.com

    Breaking change: ng:change for input[button], input[submit], input[reset], input[image]
    and button widgets is not supported any more