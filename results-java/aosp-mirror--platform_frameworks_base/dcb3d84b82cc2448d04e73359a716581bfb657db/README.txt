commit dcb3d84b82cc2448d04e73359a716581bfb657db
Author: Jim Miller <jaggies@google.com>
Date:   Thu Aug 23 19:18:12 2012 -0700

    Replace keyguard with new implementation

    This change refactors keyguard to be more modular and maintainable.  More
    specifically, it replaces the top-level view with just one device-dependent
    view that contains two views: a widget area and a security area.

    The widget area can be populated with custom widgets.

    The security area contains the current security method as dictated by
    the stored password quality.

    This change contains both the old and the new keyguard with the old keyguard
    still enabled.  The new keyguard will be enabled in a subsequent change.

    Change-Id: Id75286113771ca1407e9db182172b580f870b612