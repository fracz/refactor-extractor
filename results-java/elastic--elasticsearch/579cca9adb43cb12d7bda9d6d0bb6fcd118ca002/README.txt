commit 579cca9adb43cb12d7bda9d6d0bb6fcd118ca002
Author: Adrien Grand <jpountz@gmail.com>
Date:   Mon Sep 25 18:33:26 2017 +0200

    Allow copying from a field to another field that belongs to the same nested object. (#26774)

    The previous test was too strict and enforced that the target object was a
    parent. It has been relaxed so that fields that belong to the same nested
    object can copy to each other.

    The commit also improves error handling in case of multi-fields. The current
    validation works but may throw confusing error messages since it assumes that
    only object fields may introduce dots in fields names while multi fields may
    too.

    Closes #26763