commit 666a3835d231b3f77f907276be18b3c0086e5d12
Author: Lucas Galfaso <lgalfaso@gmail.com>
Date:   Fri Jul 11 14:01:24 2014 +0200

    refactor(bootstrap): Remove support for old bootstrap mechnanisms

    Remove support for bootstrap detection using:

    * The element id
    * The element class.

    E.g.

    ```
    <div id="ng-app">...</div>
    <div class="ng-app: module">...</div>
    ```

    Removes reference to how to bootstrap using IE7

    BREAKING CHANGE:

    If using any of the mechanisms specified above, then migrate by
    specifying the attribute `ng-app` to the root element. E.g.

    ```
    <div ng-app="module">...</div>
    ```

    Closes #8147