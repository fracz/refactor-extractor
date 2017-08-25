commit 6a16df728858de1a021d27c1406c2cf1dfd86784
Author: Lukas Reschke <lukas@statuscode.ch>
Date:   Mon Apr 24 21:11:48 2017 +0200

    Add new auth flow

    This implements the basics for the new app-password based authentication flow for our clients.
    The current implementation tries to keep it as simple as possible and works the following way:

    1. Unauthenticated client opens `/index.php/login/flow`
    2. User will be asked whether they want to grant access to the client
    3. If accepted the user has the chance to do so using existing App Token or automatically generate an app password.

    If the user chooses to use an existing app token then that one will simply be redirected to the `nc://` protocol handler.
    While we can improve on that in the future, I think keeping this smaller at the moment has its advantages. Also, in the
    near future we have to think about an automatic migration endpoint so there's that anyways :-)

    If the user chooses to use the regular login the following happens:

    1. A session state token is written to the session
    2. User is redirected to the login page
    3. If successfully authenticated they will be redirected to a page redirecting to the POST controller
    4. The POST controller will check if the CSRF token as well as the state token is correct, if yes the user will be redirected to the `nc://` protocol handler.

    This approach is quite simple but also allows to be extended in the future. One could for example allow external websites to consume this authentication endpoint as well.

    Signed-off-by: Lukas Reschke <lukas@statuscode.ch>