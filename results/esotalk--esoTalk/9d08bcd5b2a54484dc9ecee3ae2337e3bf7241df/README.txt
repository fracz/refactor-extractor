commit 9d08bcd5b2a54484dc9ecee3ae2337e3bf7241df
Author: Toby Zerner <toby.zerner@gmail.com>
Date:   Thu Apr 24 13:43:14 2014 +0930

    BREAKING changes to plugin API:
    - action methods must now be prefixed with "action_"
    - getView renamed to view, getResource renamed to resource
    - added file function
    - improved handling of settings function (don't call it every time the plugins page is loaded, but instead assume that its existence means that the plugin does have settings)