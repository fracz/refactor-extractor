commit 01d3da63cef1f82db182c6995264bf3ea3371dcc
Author: Selim Cinek <cinek@google.com>
Date:   Fri Apr 28 15:03:48 2017 -0700

    Moving the inflation to the background

    The inflation of the notifications is moved to
    the background. This should improve the general
    performance when adding / removing groups and alike.

    Test: runtest systemui
    Fixes: 34888292
    Change-Id: Ieb19a09a5a97d496d9319d917a5e317a3ad76fc4