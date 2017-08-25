commit 3e3a1affd1b8097ed942ae2364c454ae6872e201
Author: David Causse <nomoa@laposte.net>
Date:   Fri May 12 08:16:33 2017 +0200

    Always set Content-Type with Http Transport (#1302)

    Added a new param to Client::request and Request::_construct to allow
    setting the content-type of the data being sent.
    This defaults to "application/json" and is only overidden by Bulk and MultiSearch
    where it should be application/x-ndjson.

    Sadly I'm not sure how to test this properly.
    I'm not a big a big fan of adding a new param like that. Ideally we should refactor
    the $data param of Request::_construct to accept a array (defaults to application/json)
    or a new RequestBody object where the client could set the content-type alongside the
    the string.

    closes #1301