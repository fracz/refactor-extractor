commit 1ef0b078a8f690f72db8243ef4b8c207a5aa2acd
Author: Tobias Thierer <tobiast@google.com>
Date:   Sun Jun 26 16:34:46 2016 +0100

    Cleanup refactoring: Move 3 methods into DelegatingHttpsURLConnection

    These three methods were delegating to delegate; their implementation
    was duplicated across both subclasses of DelegatingHttpsURLConnection.

    This change moves them into the base class in order to
      - avoid duplication
      - separate the responsibility of delegation (base class) from the
        responsibility of change to behavior (subclasses).

    Notes:
     - Since these three methods are not available in Java 6, each of
       these need a @IgnoreJRERequirement annotation to pass the
       animal sniffer test. This appears like it should have been
       necessary before but for some reason it used to pass without.
     - This in turn requires a new dependency from okhttp-urlconnection
       onto animal-sniffer-annotations. The dependency was added onto
       version 1.11 of that library (consistent with okhttp itself);
       okio depends on version 1.10, the latest version is 1.15.