commit 694bd72611e055c2916f44c10d9a7aca7eb2b6d8
Merge: 6a51831 ef3ae9c
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Dec 26 08:59:17 2013 +0100

    Merge branch '2.4'

    * 2.4: (44 commits)
      [FrameworkBundle] Add missing license headers
      Fix parent serialization of user object
      [DependencyInjection] fixed typo
      added condition to avoid skipping tests on JSON_PRETTY support
      add memcache, memcached, and mongodb extensions to run skipped tests
      [DependencyInjection] Fixed support for backslashes in service ids.
      fix #9356 [Security] Logger should manipulate the user reloaded from provider
      [FrameworkBundle] Added extra details in XMLDescriptor to improve container description
      fixed CS
      Crawler default namespace fix
      [BrowserKit] fixes #8311 CookieJar is totally ignorant of RFC 6265 edge cases
      [HttpFoundation] fixed constants that do exist in 2.3 (only in 2.4)
      fix 5528 let ArrayNode::normalizeValue respect order of value array provided
      fix #7243 allow 0 as arraynode name
      Fixed issue in BaseDateTimeTransformer when invalid timezone cause Transformation filed exception (closes #9403).
      BinaryFileResponse should also return 416 or 200 on some range-requets
      fix deprecated usage and clarify constructor defaults for number formatter
      Bumping dependency to ProxyManager to allow testing against the new 0.5.x branch changes
      Do normalization on tag options
      bumped Symfony version to 2.3.9
      ...