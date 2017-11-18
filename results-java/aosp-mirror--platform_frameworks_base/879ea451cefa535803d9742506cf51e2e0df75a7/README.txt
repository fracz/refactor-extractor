commit 879ea451cefa535803d9742506cf51e2e0df75a7
Author: Mark Doliner <markdoliner@google.com>
Date:   Thu Jan 2 12:38:07 2014 -0800

    Fix documentation for Parcel.readException().

    The old documentation was kind of useless and not clear, so this is an
    improvement.

    In addition to this change, I think this method should probably be private
    instead of public, especially since the values for code are private. But it's
    too late to change that now. Also the method name is pretty poor, since it's
    not actually reading anything. Something like "throwException" would be more
    appropriate.

    I'm open to suggestions on how to improve this situation. Maybe mark the
    method as deprecated with a comment that it will be made private in the
    future?

    Change-Id: I830f2bcf606714bd130d8c953aa33974b33c9a83