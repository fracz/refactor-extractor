commit d39fd5a97c6f56794a6ed7ac1dfb0bbf585becf1
Author: Henrik Baard <henrik.baard@sonyericsson.com>
Date:   Thu Nov 18 14:08:36 2010 +0100

    Changing connect and response timeout.

    In bad network conditions and where switches often occur between 2G and
    3G the timeout of 20s is too short.

    Setting this timeout to 60 seconds will improve functionality in bad conditions
    while it will not affect functionality in good networks. This change also aligns
    the timeouts with the timeouts used by the Browser (Connection.java).

    Change-Id: I0fbe3cbfe734f8d55a41bfa5d8ab6b332a19f912