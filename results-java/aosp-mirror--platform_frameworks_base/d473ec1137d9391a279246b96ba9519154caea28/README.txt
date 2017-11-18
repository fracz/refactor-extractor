commit d473ec1137d9391a279246b96ba9519154caea28
Author: Felipe Leme <felipeal@google.com>
Date:   Thu Feb 16 08:14:48 2017 -0800

    Added missing calls to onConnected() and onDisconnected().

    They got lost in a previous refactoring....

    Bug: 35395043
    Test: CtsAutoFillServiceTestCases pass
    Test: manual verification
    Change-Id: I4e70f84185d6708ea0ebfa831c160e859fcf9e5c