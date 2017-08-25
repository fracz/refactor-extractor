commit 0875cc18e5f1fb1758342c283ef9826bd3cae71a
Author: Frederic Massart <fred@moodle.com>
Date:   Tue Dec 8 16:55:02 2015 +0800

    MDL-52423 tool_lp: Allow null related objects in exporters

    Note that the related objects MUST always ALL be passed to the
    constructor when instantiating objets, even if they are null.

    Related objects were introduced to improve performance and their
    requirement is one way to ensure that developers don't forget them.