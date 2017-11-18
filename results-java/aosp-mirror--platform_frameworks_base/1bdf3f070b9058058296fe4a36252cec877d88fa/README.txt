commit 1bdf3f070b9058058296fe4a36252cec877d88fa
Author: Felipe Leme <felipeal@google.com>
Date:   Tue Feb 7 13:40:55 2017 -0800

    Set autofill id on virtual structures.

    ViewStructure.newChild(virtualId) uses the AutoFillId of the parent,
    which was not set anymore due do a previous refactoring, and was
    causing this method to crash a custom view when
    onProvideAutoFillVirtualStructure() was called on it.

    Test: manual verification
    Bug: 31001899

    Change-Id: I602b421b0ec3a926ffdd52253d5b2498c4217d02