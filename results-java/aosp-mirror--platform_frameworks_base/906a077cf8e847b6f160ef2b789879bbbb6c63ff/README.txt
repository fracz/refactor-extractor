commit 906a077cf8e847b6f160ef2b789879bbbb6c63ff
Author: Paul Stewart <pstew@google.com>
Date:   Fri Feb 24 10:21:35 2017 -0800

    Fix up EAP-SIM documentation

    Address API Council comments on doucmentation for the method calls
    and constants related to EAP-SIM.  While here, improve unit tests
    to ensure that passing a null certificate (chain) causes the config
    to forget any existing client certificates.

    Bug: 35847887
    Test: Unit tests

    Change-Id: I1c4e18e1a7cfb61aa4764e32778793368938e70b