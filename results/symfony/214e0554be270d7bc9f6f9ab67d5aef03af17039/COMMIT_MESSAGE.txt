commit 214e0554be270d7bc9f6f9ab67d5aef03af17039
Merge: 8cc0cb7 dc3a680
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Jul 22 09:50:28 2012 +0200

    merged branch bschussek/renderer (PR #5006)

    Commits
    -------

    dc3a680 [Form] Improved FormRenderer API to reduce the size of the function call stack during rendering

    Discussion
    ----------

    [Form] Improved FormRenderer API to decrease the function call stack

    Bug fix: no
    Feature addition: no
    Backwards compatibility break: **yes**
    Symfony2 tests pass: yes
    Fixes the following tickets: #4962, #4973
    Todo: -

    This PR reduces the function call stack size when rendering by directly calling the methods `renderBlock` and `searchAndRenderBlock` (formerly `renderSection`) and removing the delegating methods `render(Widget|Label|Row|...)`.

    It breaks BC in that PHP templates now need to pass the FormView instance to `block` (formerly `renderBlock`). This is necessary, otherwise that function may behave buggy in special circumstances.

    Otherwise this PR cleans up API method and parameter names to improve clarity.