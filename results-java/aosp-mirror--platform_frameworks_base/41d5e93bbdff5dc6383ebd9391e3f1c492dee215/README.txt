commit 41d5e93bbdff5dc6383ebd9391e3f1c492dee215
Author: Derek Sollenberger <djsollen@google.com>
Date:   Mon Aug 23 14:51:41 2010 -0400

    Move APK monitoring into WebView.

    This CL adds the monitoring logic that was removed from the Browser
    in a companion CL.  This allows applications other than the Browser
    to use special features offered by YouTube and plugins. Additionally,
    the pluginManager was refactored to prevent code duplication between
    the manager and WebView.

    Change-Id: Ie37f248c8edd9d06ae1fd6675dd5f06f04774b09
    http://b/2834603