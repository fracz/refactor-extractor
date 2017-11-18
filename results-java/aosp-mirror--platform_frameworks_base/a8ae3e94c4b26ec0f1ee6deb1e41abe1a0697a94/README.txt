commit a8ae3e94c4b26ec0f1ee6deb1e41abe1a0697a94
Author: John Reck <jreck@google.com>
Date:   Wed Jun 13 10:37:40 2012 -0700

    Call WebView.performLongClick instead of performLongClick()

     Bug: 6656538
     Due to the WebView/WebViewClassic refactor we need to call
     WebView.performLongClick instead of performLongClick directly
     to allow subclasses to override performLongClick

    Change-Id: I9b580217fbafc82d03e63eabfdda9f5bad98db0f