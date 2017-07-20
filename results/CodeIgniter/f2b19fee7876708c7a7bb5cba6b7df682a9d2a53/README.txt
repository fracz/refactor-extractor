commit f2b19fee7876708c7a7bb5cba6b7df682a9d2a53
Author: Andrey Andreev <narf@bofh.bg>
Date:   Wed Oct 31 16:16:24 2012 +0200

    Multiple improvements to the URI class

    (thanks to @sourcejedi, PR #1326 for most of the ideas)

     - Renamed _detect_uri() and _parse_cli_args() to _parse_request_uri() and _parse_argv() respectively.
     - Added _parse_query_string() which allows us to detect the URI path from QUERY_STRING much like it is done in _parse_request_uri().

    (the above changes also allow for a simpler logic in the case where the *uri_protocol* setting is not set to 'AUTO')

     - Updated application/config/config.php with a better list of the *uri_protocol* options.
     - Added _reset_query_string() to aid in re-processing  from the QUERY_STRING (utilized in _parse_request_uri() and _parse_query_string()).