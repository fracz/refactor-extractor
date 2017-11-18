commit cc38f5dbcb6cd8ee23215589015087a7ecf7a871
Author: Peter Edwards <sweetlilmre@gmail.com>
Date:   Thu Oct 17 14:38:59 2013 +0200

    Major refactor on current code base including:

    - Rework of the handler hierarchy to support a single base class and remove instanceof checks.
    - A full implementation of Cancel behaviour with granular cancellation time (downloads are now chunked so can be canceled before completion).
    - Progress and retry notification
    - Fixed some small stability issues (unhandled exception, connection issues on network switch).
    - SyncHttpClient fixes to use any handler (all handlers can now be forced into sync mode)
    - Cleanup of JsonHttpResponseHandler.java removing all superfluous helper methods

    Ideally JsonHttpResponseHandler.java, TextHttpResponseHandler.java, BinaryHttpResponseHandler.java should be moved into the examples directory