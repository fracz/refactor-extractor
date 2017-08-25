commit d8ada370d76acc10e602d25db6af5be936880d4c
Author: Thomas Müller <thomas.mueller@tmit.eu>
Date:   Tue Oct 1 13:25:58 2013 +0200

    Squashed commit of the following:

    commit ae1f68ac54cf2878d265b2bbce13bd600d2d0719
    Author: Thomas Müller <thomas.mueller@tmit.eu>
    Date:   Thu Aug 22 11:45:27 2013 +0200

        fixing undefined variable

    commit 982f327ca10eea0a2222eae3e74210648591fd8a
    Author: Thomas Müller <thomas.mueller@tmit.eu>
    Date:   Wed Aug 7 12:00:14 2013 +0200

        adding login.php as alternative for index.php/login

    commit da0d7e1d096fb80789524b01f0f96fe08d147943
    Author: Thomas Müller <thomas.mueller@tmit.eu>
    Date:   Wed Aug 7 11:36:12 2013 +0200

        adding a route for web login

    commit 8e2a01160485cf7e9a2eb8bf46f06fae73956e8e
    Author: Karl Beecher <karl@endocode.com>
    Date:   Tue Aug 6 17:00:28 2013 +0200

        Login attempt returns true instead of exiting immediately

    commit fd89d55de9e71e986e03a0de9aad9407b632e22f
    Author: Karl Beecher <karl@endocode.com>
    Date:   Mon Aug 5 15:31:30 2013 +0200

        Further abstraction.

        This change introduces the ApacheBackend interface for backends that
        depend on Apache authentication and session management. There are no
        longer references to specific backends in OC_User.

    commit 469cfd98aea5a37985722cf5f9e00ece0ce38178
    Author: Karl Beecher <karl@endocode.com>
    Date:   Thu Aug 1 15:46:36 2013 +0200

        Make login attempt function protected.

    commit d803515f19ff086e2028fcaa51afae579685e596
    Author: Karl Beecher <karl@endocode.com>
    Date:   Wed Jul 31 16:00:22 2013 +0200

        Amends the login link

        When using a Shibboleth login, clicking logout displays a message to the
        user instead of ending the session.

    commit aa8c1fcea05c8268f26a10b21c4e0bc547c3414f
    Author: Karl Beecher <karl@endocode.com>
    Date:   Tue Jul 30 13:15:59 2013 +0200

        Abstract Shibboleth authentication into an Apache authentication method

    commit 69082f2ebcab267f6e8eceb1a252f84c52236546
    Author: Karl Beecher <karl@endocode.com>
    Date:   Tue Jul 30 11:22:26 2013 +0200

        Convert spaces -> tabs

    commit 5a80861d86855eec5906fd5e235ac4ff12efb0f2
    Author: Karl Beecher <karl@endocode.com>
    Date:   Mon Jul 29 17:40:48 2013 +0200

        Separate the authentication methods

        SABRE authentication and base authentication have slightly different
        workings right now. They should be refactored into a common method
        later, but time pressure requires us to reinvent the wheel slightly.

    commit dc20a9f8764b103b7d8c5b713f2bcdae18708b65
    Author: Karl Beecher <karl@endocode.com>
    Date:   Mon Jul 29 17:07:07 2013 +0200

        Authenicate calls to WebDAV against Shibboleth.

        When using WebDAV, the OC_Connector_Sabre_Auth::authenticate method is
        normally called without trying the Shibboleth authentication... thus the
        session is not established.

        The method now tries Shib authentication, setting up a session if the
        user has already authenticated.

    commit 091e4861b2246c4084c9b30e232289fde4ba1abf
    Author: Karl Beecher <karl@endocode.com>
    Date:   Mon Jul 29 14:04:54 2013 +0200

        Sets up the Shibboleth login attempt.

    commit bae710ec0579ef99b23022cc12f6876c5fe6b0d5
    Author: Karl Beecher <karl@endocode.com>
    Date:   Mon Jul 29 12:36:44 2013 +0200

        Add a method for attempting shibboleth login.

        If the PHP_AUTH_USER and EPPN environment variables are set, attempt a
        Shibboleth (passwordless) login.

    commit 667d0710a7854e58fb109201d9cee6ec064e793a
    Author: Karl Beecher <karl@endocode.com>
    Date:   Mon Jul 29 11:38:04 2013 +0200

        Revert "Adds the apps2 folder with user_shibboleth backend."

        This reverts commit 7abbdb64676d667b0c69aca37becdc47e56dc7ef.

    commit 7abbdb64676d667b0c69aca37becdc47e56dc7ef
    Author: Karl Beecher <karl@endocode.com>
    Date:   Mon Jul 29 11:28:06 2013 +0200

        Adds the apps2 folder with user_shibboleth backend.

    Conflicts:
            core/templates/layout.user.php
            lib/base.php