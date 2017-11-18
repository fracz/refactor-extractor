commit 03e489157191a7bda903babe9437076bf135bc41
Author: Derek Sollenberger <djsollen@google.com>
Date:   Tue May 18 17:03:42 2010 -0400

    Refactoring more webview zoom code into ZoomManager.

    This CL is #2 of multiple CL's to remove the zoom code
    from webview and place it into webkit.ZoomManager. Each
    commit is only part of the entire refactoring so there
    are many ZoomManager.* variables still referenced from
    WebView.

    Change-Id: I21f6517dff46b65a277bc67120ffabe043098e5e
    http://b/2671604