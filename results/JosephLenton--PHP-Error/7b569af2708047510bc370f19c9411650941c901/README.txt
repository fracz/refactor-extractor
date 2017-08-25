commit 7b569af2708047510bc370f19c9411650941c901
Author: joseph lenton <jl235@kent.ac.uk>
Date:   Wed Jun 13 07:29:52 2012 +0100

    improved errors and JS wrapping code

    Added wrapper for XMLHTTPRequest handler, added syntax highlighting to
    require/include error messages  and '$this used outside object context',
    and fixed missing stack trace for require errors.