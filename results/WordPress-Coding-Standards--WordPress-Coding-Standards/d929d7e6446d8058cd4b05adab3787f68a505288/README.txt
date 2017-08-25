commit d929d7e6446d8058cd4b05adab3787f68a505288
Author: jrfnl <jrfnl@users.noreply.github.com>
Date:   Tue Jun 20 05:05:50 2017 +0200

    OperatorSpacing sniff: Defer to upstream sniff for improved results.

    Up to now, this sniff was based on a copy of a very old and outdated version of the upstream sniff.
    The upstream and downstream sniffs had diverged quite widely code-wise, but not intention-wise.

    I've run both the `WordPress.WhiteSpace.OperatorSpacing` sniff as well as its upstream version `Squiz.WhiteSpace.OperatorSpacing` over both the WPCS unit tests for this sniff as well as the Squiz unit tests for this sniff.

    While there were differences in the results, they were for the better in favour of the upstream version of the sniff.