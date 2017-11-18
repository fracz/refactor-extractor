commit 6875d472092a4f40865f590e68a24be18727d4c3
Author: Matthew Ng <ngmatthew@google.com>
Date:   Mon May 1 13:59:50 2017 -0700

    Stack is visible if behind docked which is behind pinned stack (1/2)

    Handles the case if pip is shown with splitscreen when the stack order
    is pinned, docked, current stack, etc (top to bottom). Added a condition
    to check to make sure that this order of stacks allows the current stack
    to be visible. Also added a condition to not hide docked stack when pip
    appears (this only occurs with command line or cts tests); added TODO
    for refactor.

    Change-Id: I53bd55014c08c60f360b95ed7100ef49778f891b
    Fixes: 37294521
    Test: run-test CtsServicesHostTestCases
    android.server.cts.ActivityManagerPinnedStackTests#
    testPinnedStackWithDockedStack