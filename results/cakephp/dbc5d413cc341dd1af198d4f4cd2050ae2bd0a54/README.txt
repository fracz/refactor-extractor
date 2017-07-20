commit dbc5d413cc341dd1af198d4f4cd2050ae2bd0a54
Author: Mark Story <mark@mark-story.com>
Date:   Mon Sep 12 00:17:51 2016 -0400

    Start refactoring URL handling to use PSR7 implementation.

    Convert the internals of URL handling in the request class to use
    the UriInterface implementation in diactoros. This will let us remove
    a bunch of code from `Request` and replace it with code from diactoros
    and our own ServerRequestFactory.

    There are still a bunch of rough edges. Particularily around how the
    base URL and webroot directory are being handled.