commit 35482babd9eb0970edbc99f223e04594a9d09364
Author: Michał Gołębiowski <m.goleb@gmail.com>
Date:   Mon Oct 17 23:08:41 2016 +0200

    refactor($sniffer): remove $sniffer.vendorPrefix

    Previously, Angular tried to detect the CSS prefix the browser supports and
    then use the saved one. This strategy is not ideal as currently some browsers
    are supporting more than one vendor prefix. The best example is Microsoft Edge
    that added -webkit- prefixes to be more Web-compatible; Firefox is doing
    a similar thing on mobile. Some of the -webkit--prefixed things are now even
    getting into specs to sanction that they're now required for Web compatibility.

    In some cases Edge even supports only the -webkit--prefixed property; one
    example is -webkit-appearance.

    $sniffer.vendorPrefix is no longer used in Angular core outside of $sniffer
    itself; taking that and the above problems into account, it's better to just
    remove it. The only remaining use case was an internal use in detection of
    support for transitions/animations but we can directly check the webkit prefix
    there manually; no other prefix matters for them anyway.

    $sniffer is undocumented API so this removal is not a breaking change. However,
    if you've previously been using it in your code, just paste the following
    to get the same function:

        var vendorPrefix = (function() {
          var prefix, prop, match;
          var vendorRegex = /^(Moz|webkit|ms)(?=[A-Z])/;
          for (prop in document.createElement('div').style) {
            if ((match = vendorRegex.exec(prop))) {
              prefix = match[0];
              break;
            }
          }
          return prefix;
        })();

    The vendorPrefix variable will contain what $sniffer.vendorPrefix used to.

    Note that we advise to not check for vendor prefixes this way; if you have to
    do it, it's better to check it separately for each CSS property used for the
    reasons described at the beginning. If you use jQuery, you don't have to do
    anything; it automatically adds vendor prefixes to CSS prefixes for you in
    the .css() method.

    Fixes #13690
    Closes #15287