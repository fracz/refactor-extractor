commit 4cc68d8256ab60a293d8d5434807ed759c34e61b
Author: Marvin S. Addison <marvin.addison@gmail.com>
Date:   Sat Jun 8 09:30:15 2013 -0400

    CAS-1238 Port handlers to new AuthenticationHandler interface.

    1. Port all existing AuthenticationHandler components to new AH interface.
       This afforded some opportunity for relatively minor refactoring.
    2. Move CAS 3.0 AH interface, related 3.0 components, and
       LegacyAuthenticationHandlerAdapter to legacy module to provide
       transition path for third-party authentication handlers.
    3. Remove CAS 2.x transitional components from legacy module.
    4. Remove deprecated authentication-related components.