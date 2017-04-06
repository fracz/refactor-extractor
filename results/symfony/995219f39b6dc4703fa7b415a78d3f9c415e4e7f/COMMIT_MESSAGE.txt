commit 995219f39b6dc4703fa7b415a78d3f9c415e4e7f
Merge: 18495e7 6c67476
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Nov 29 12:29:12 2012 +0100

    Merge branch '2.1'

    * 2.1:
      replaced magic strings by proper constants
      refactored tests for Request
      fixed the logic in Request::isSecure() (if the information comes from a source that we trust, don't check other ones)
      added a way to configure the X-Forwarded-XXX header names and a way to disable trusting them
      fixed algorithm used to determine the trusted client IP
      removed the non-standard Client-IP HTTP header

    Conflicts:
            src/Symfony/Component/HttpFoundation/Tests/RequestTest.php