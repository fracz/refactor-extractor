commit 9eb2a89f88bd4b62fb997c77372683b0805fd7c9
Author: Iain Merrick <husky@google.com>
Date:   Thu Nov 18 15:32:31 2010 +0000

    Make maybeSavePassword() a top-level method in BrowserFrame.

    We need to be able to reuse this logic from the Chrome HTTP
    stack. This is just a refactoring that doesn't change any
    behaviour; we'll need a separate change in external/webkit
    to call this from the code that handles POST requests.

    Change-Id: I3f3f84a15a8501f63de974bffa6f7c911a3d3820