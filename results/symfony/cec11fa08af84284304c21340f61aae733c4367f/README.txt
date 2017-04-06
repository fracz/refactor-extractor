commit cec11fa08af84284304c21340f61aae733c4367f
Merge: 4860e75 8f33f2e
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Nov 19 13:32:16 2012 +0100

    Merge branch '2.1'

    * 2.1:
      [Routing] made it compatible with older PCRE version (pre 8)
      tiny refactoring for consistency
      fixed docblock return type
      Added HttpCache\Store::generateContentDigest() + changed visibility

    Conflicts:
            src/Symfony/Component/Routing/Matcher/Dumper/ApacheMatcherDumper.php
            src/Symfony/Component/Routing/Matcher/Dumper/PhpMatcherDumper.php
            src/Symfony/Component/Routing/Tests/Fixtures/dumper/url_matcher1.php
            src/Symfony/Component/Routing/Tests/Fixtures/dumper/url_matcher2.php
            src/Symfony/Component/Routing/Tests/Fixtures/dumper/url_matcher3.php
            src/Symfony/Component/Routing/Tests/RouteCompilerTest.php