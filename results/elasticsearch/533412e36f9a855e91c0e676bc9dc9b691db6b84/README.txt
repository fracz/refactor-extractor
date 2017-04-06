commit 533412e36f9a855e91c0e676bc9dc9b691db6b84
Author: Jason Tedor <jason@tedor.me>
Date:   Wed Aug 3 23:02:13 2016 -0400

    Improve cat thread pool API

    Today, when listing thread pools via the cat thread pool API, thread
    pools are listed in a column-delimited format. This is unfriendly to
    command-line tools, and inconsistent with other cat APIs. Instead,
    thread pools should be listed in a row-delimited format.

    Additionally, the cat thread pool API is limited to a fixed list of
    thread pools that excludes certain built-in thread pools as well as all
    custom thread pools. These thread pools should be available via the cat
    thread pool API.

    This commit improves the cat thread pool API by listing all thread pools
    (built-in or custom), and by listing them in a row-delimited
    format. Finally, for each node, the output thread pools are sorted by
    thread pool name.

    Relates #19721