commit 862b2a2dc3815ed11d7fa95cdf99d84e45902957
Author: mark_story <mark@mark-story.com>
Date:   Sat Nov 16 16:12:14 2013 -0500

    Add count() and improve one() to ResultSet

    one() should work even if the ResultSet has been serialized. Adding
    support for count() as other parts of the framework expect that result
    sets are countable.