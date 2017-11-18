commit 4fe4a409bca01e16c807f3f76633afc1d9797e2a
Author: Muyuan Li <muyuanli@google.com>
Date:   Wed Mar 30 16:50:11 2016 -0700

    sysui: refactor for extensibility.

    1. Refactored container height calculation out of updateBottom of QSContainer;

    2. Added a method in QSContainer to return min expansion height.

    Bug: 27929808
    Change-Id: I5a6d04ffb225bccfc56fa5f4ab98d330ed763a8c
    (cherry picked from commit 9e874950195d6b76a64e28c236e8d109d551281f)