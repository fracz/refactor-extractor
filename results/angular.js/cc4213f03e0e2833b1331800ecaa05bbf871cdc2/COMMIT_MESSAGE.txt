commit cc4213f03e0e2833b1331800ecaa05bbf871cdc2
Author: Peter Bacon Darwin <pete@bacondarwin.com>
Date:   Thu Mar 19 14:26:24 2015 +0000

    chore(docs): improve error doc layout and linking

    You can now link to an error by its name, namespace:name or error:namespace:name.
    For example these would all link to https://docs.angularjs.org/error/$compile/ctreq

    ```
    {@link ctreq}
    {@link $compile:ctreq}
    {@link error:$compile:ctreq}
    ```