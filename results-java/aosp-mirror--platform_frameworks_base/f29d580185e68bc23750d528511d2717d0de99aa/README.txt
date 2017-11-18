commit f29d580185e68bc23750d528511d2717d0de99aa
Author: Fred Quintana <>
Date:   Wed Apr 8 19:14:54 2009 -0700

    AI 145177: phase two of the AccountManager
      - added an AccountManagerActivity, a base Activity that can be
      used by activities that are launched by AccountAuthenticator
      intents. This makes it easy for an Activity to send a result
      using an AccountAuthenticatorResponse
      - added debug strings to the AccountAuthenticatorCache
      - improved the API for the AccountAuthenticatorResponse and
      made it Parcelable so that it can be passed to an Activity
      via an Intent
      - changed the AccountManager to use Futures for the
      asynchronous calls and to notify the user via a callback
      when the request is complete
      - changed the AccountManager to convert any errors that are
      returned into Exceptions
      - added constants for the error codes that are passed across
      the IAccountManagerResponse and
      IAccountAuthenticatorResponse interfaces
      - added a dump() method to the AccountManagerService so that
      it can display the list of active sessions and registered
      authenticators
      - added an way to interrogate the AccountManagerService for
      the list of registered authenticators
      - removed more methods from the GoogleLoginServiceHelper and
      GoogleLoginServiceBlockingHelper and changed the callers to
      use the AccountManager

    Automated import of CL 145177