commit 36a547b852a7d254535e2d203cffe10f2e5c5617
Author: Vojta Jina <vojta.jina@gmail.com>
Date:   Tue Aug 26 12:15:50 2014 -0700

    refactor: remove doReload arg used only for testing

    We run unit tests in “strict” mode and thus can’t monkey-patch `window.location` nor `window.location.reload`. In order to avoid full page reload, we could pass location as argument, or another level of indirection, something like this:
    ```js
    var ourGlobalFunkyLocation = window.location;
    function reloadWithDebugInfo() {
      window.name = 'NG_ENABLE_DEBUG_INFO!' + window.name;
      ourGlobalFunkyLocation.reload();
    }

    // in the test
    ourGlobalFunkyLocation = {
      reload: function() {}
    };
    reloadWithDebugInfo();
    ourGlobalFunkyLocation = window.location;
    ```

    I don’t think any of these make sense, just so that we can test setting `window.name`. If the `reloadWithDebugInfo` function was more complicated, I would do it.

    I don’t think it’s worthy to confuse production code with extra logic which purpose was only to make testing possible.