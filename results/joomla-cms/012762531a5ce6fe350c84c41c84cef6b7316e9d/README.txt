commit 012762531a5ce6fe350c84c41c84cef6b7316e9d
Author: andrepereiradasilva <andrepereiradasilva@users.noreply.github.com>
Date:   Sun Oct 2 17:52:13 2016 +0100

    Move keepalive to external js (#8545)

    * Keepalive with data attributes

    * cs

    * cs

    * cs empty line

    * Not seconds ...

    * object

    * No double entities

    We have to use JRoute without xhtml or else the admin keep alive URL get
    double entities `&amp;amp;?`

    * From chocolate to vanilla ...

    Works with vanilla js!
    Works if onload event is already fired when script is executed.
    This way the js file can be loaded on render, asyncronous or deferered
    to after the onload event.
    Should be cross-browser compatible.

    * improvements

    * cs

    * default options, move to jui folder, asynchronous loading

    * simple correct to work with new script attributes PR (11289). Adds auto version hash.

    * update to match #11289 JHtml script behaviour

    * use __DEPLOY_VERSION__

    * cs and simplify

    * Update behavior.php

    * Update keepalive.js

    * Update keepalive.min.js

    * Update keepalive.js

    * Update keepalive.min.js

    * fix conflicts 1

    * use new addScriptOptions and Joomla.request methods

    * fix unit tests

    * fix last unit test