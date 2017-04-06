commit 889c422d6e6a890c5c2e9e45388ac82c45105e4f
Merge: 36a63be 041a2e9
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Apr 24 22:04:25 2011 +0200

    Merge remote branch 'igorw/ipv6'

    * igorw/ipv6:
      [HttpFoundation] minor optimization
      minor adjustments suggested by vicb
      [HttpFoundation] IPv6 support for RequestMatcher
      [HttpFoundation] refactor RequestMatcherTest to use dataProvider
      [Validator] use full iPv6 regex
      [Validator] add IPv6 support to UrlValidator
      [HttpFoundation] add IPv6 support to Request
      [HttpFoundation] test Request::create with an IP as host name
      [HttpFoundation] refactor Request::getClientIp test