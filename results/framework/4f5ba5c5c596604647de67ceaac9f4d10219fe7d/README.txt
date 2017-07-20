commit 4f5ba5c5c596604647de67ceaac9f4d10219fe7d
Author: Todd Christensen <tchristensen@bykd.com>
Date:   Thu Mar 26 10:35:02 2015 -0700

    Routing: cache URL root and scheme per request.

    This cuts down on time spent determining route URLs, which can end up
    being a lot depending on the application (for example, lists, navigation,
    etc. may contain many links.)

    In an application that generates about 150 total URLs on a list page, this
    improves runtime by over 7%.