commit 85844916b8a7cc7f6aabc6c37af7380a4c000bcb
Author: Jeff Sharkey <jsharkey@android.com>
Date:   Thu Nov 13 16:20:38 2014 -0800

    Block loading WebView in privileged processes.

    WebView is very powerful, but it also has a large attack area.  To
    improve security, refuse to load WebView components when running as
    the root or system UIDs.

    Bug: 18376908
    Change-Id: I515b819033586076b1a9668023bb43ee0295d003