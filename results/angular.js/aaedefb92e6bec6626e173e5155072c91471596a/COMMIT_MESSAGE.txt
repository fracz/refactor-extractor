commit aaedefb92e6bec6626e173e5155072c91471596a
Author: Igor Minar <igor@angularjs.org>
Date:   Mon Mar 26 15:43:56 2012 -0700

    refactor($sniffer): make $sniffer service private

    This service has been accidentaly documented in the past, it should not be considered
    to be public api.

    I'm also removing fallback to Modernizr since we don't need it.

    Breaks any app that depends on this service and its fallback to Modernizr, please
    migrate to custom "Modernizr" service:

        module.value('Modernizr', function() { return Modernizr; });