commit fc45c5732e7db7b7eeb0221f8ee7efccae7a994a
Author: Zachary Tong <zacharyjtong@gmail.com>
Date:   Tue Nov 12 14:05:54 2013 -0500

    Bring CurlMultiConnection up-to-date

    Big refactor to bring CurlMultiConnection up to date with the
    GuzzleConnection class.  Throws appropriate exceptions, passes
    yaml test suite.

    Still not recommended for general use, but may be useful for
    situations which require low-latency initialization, such as
    autocomplete.