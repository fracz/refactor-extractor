commit 7bb0ff0b90f9e2f62ddfa150f96710f2c78a7dc5
Author: Jonathan Gerrish <jongerrish@google.com>
Date:   Tue Dec 22 16:26:39 2015 -0800

    Removed tests that asserted native methods blow up with assertion errors if called. In API 23 nativePollOnce has reverted back to an instance method and refactoring these tests again to test implementation details feels like a waste of time and this commit removes some messy testing infrastructure. I'd rather we just test behaviour of API methods.