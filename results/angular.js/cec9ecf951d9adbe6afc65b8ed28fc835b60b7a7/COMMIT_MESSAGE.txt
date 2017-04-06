commit cec9ecf951d9adbe6afc65b8ed28fc835b60b7a7
Author: Vojta Jina <vojta.jina@gmail.com>
Date:   Tue Aug 26 17:21:05 2014 -0700

    refactor($compile): $$addScopeInfo always expects jq wrapper

    `$$addScopeInfo` used to accept either DOM Node or jqLite/jQuery
    wrapper. This commit simplifies the method to always require
    jqLite/jQuery wrapper and thus remove the `element.data` condition which
    was wrong. If `element` was a raw comment element, the `data` property
    was a string (the value of the comment) and an exception was thrown.