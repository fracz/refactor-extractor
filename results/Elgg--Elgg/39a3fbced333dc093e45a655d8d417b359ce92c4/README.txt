commit 39a3fbced333dc093e45a655d8d417b359ce92c4
Author: Steve Clay <steve@mrclay.org>
Date:   Tue Apr 26 13:50:24 2016 -0400

    feature(js): elgg/Ajax users get more access to underlying resources

    All success/fail/complete callbacks and Promise handlers now receive the standard
    jQuery arguments, including `jqXHR`, `textStatus`, and `errorThrown`.

    In successful responses, `jqXHR.AjaxData` contains the wrapper object passed
    through the `ajax_response` hooks, and `jqXHR.AjaxData.status` is set to
    0 or -1 (indicating `register_error()` was called on the server).

    The default error handler is improved so that server-encoded messages (sent
    from `register_error`/`system_message`) are displayed even on HTTP error
    responses. Note the API to send this type of responses has not yet been
    implemented, but is planned.

    Updates ElggAjaxTest to use jquery-mockjax for better coverage and tests new
    features.

    Makes developer/ajax_demo a little more like an integration test.

    Fixes #9767