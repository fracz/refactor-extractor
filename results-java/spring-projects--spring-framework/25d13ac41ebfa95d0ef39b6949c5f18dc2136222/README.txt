commit 25d13ac41ebfa95d0ef39b6949c5f18dc2136222
Author: Sam Brannen <sam@sambrannen.com>
Date:   Thu Oct 2 14:03:39 2014 +0200

    Sensible defaults for Servlet & Filter registrations in mock

    Prior to this commit, the getter methods in MockServletContext threw an
    UnsupportedOperationException when trying to retrieve Servlet and
    Filter registrations.

    This commit improves the behavior of these methods by returning null
    when a single registration is requested and an empty map when all
    registrations are requested. This is now in line with the Javadoc for
    ServletContext. Note, however, that the corresponding setter methods
    still throw UnsupportedOperationExceptions which is suitable behavior
    for a mock.

    Issue: SPR-12290