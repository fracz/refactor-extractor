commit 52454c61993e4d4376d429652104ae404503c3ad
Author: Neil Fuller <nfuller@google.com>
Date:   Tue Jan 6 11:57:34 2015 +0000

    Refactoring: Make RouteSelector independent of Connection

    Ultimate goal: to improve the TLS fallback behavior so that
    socket connections will not be created if the necessary
    TLS protocols will not be supported.

    To achieve this RouteSelector will be moved into Connection
    and so that it can be passed the Socket or protocol information
    during the route selection process.

    This is a small first step: Make RouteSelector independent
    of Connection so that it can later be moved inside of
    Connection. It puts the RouteSelector interface in terms
    of Routes, which seems logical.