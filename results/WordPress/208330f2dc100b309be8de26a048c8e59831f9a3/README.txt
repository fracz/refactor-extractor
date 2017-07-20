commit 208330f2dc100b309be8de26a048c8e59831f9a3
Author: Weston Ruter <weston@xwp.co>
Date:   Fri Sep 25 21:02:27 2015 +0000

    Customizer: Defer embedding widget controls to improve DOM performance and initial load time.

    The Menu Customizer feature includes a performance technique whereby the controls for nav menu items are only embedded into the DOM once the containing menu section is expanded. This commit implements the same DOM deferral for widgets but goes a step further than just embedding the controls once the widget area's Customizer section is expanded: it also defers the embedding of the widget control's form until the widget is expanded, at which point the `widget-added` event also fires to allow any additional widget initialization to be done. The deferred DOM embedding can speed up initial load time by 10x or more. This DOM deferral also yields a reduction in overall memory usage in the browser process.

    Includes changes to `wp_widget_control()` to facilitate separating out the widget form from the surrounding accordion container; also includes unit tests for this previously-untested function. Also included are initial QUnit tests (finally) for widgets in the Customizer.

    Fixes #33901.

    Built from https://develop.svn.wordpress.org/trunk@34563


    git-svn-id: http://core.svn.wordpress.org/trunk@34527 1a063a9b-81f0-0310-95a4-ce76da25c4cd