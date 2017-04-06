commit 6c67476ef049d52583ececd581cb44f80bca55cf
Merge: 922c201 e5536f0
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Nov 29 12:27:48 2012 +0100

    Merge branch '2.0' into 2.1

    * 2.0:
      replaced magic strings by proper constants
      refactored tests for Request
      fixed the logic in Request::isSecure() (if the information comes from a source that we trust, don't check other ones)
      added a way to configure the X-Forwarded-XXX header names and a way to disable trusting them
      fixed algorithm used to determine the trusted client IP
      removed the non-standard Client-IP HTTP header

    Conflicts:
            src/Symfony/Component/HttpFoundation/Request.php
            src/Symfony/Component/HttpFoundation/Tests/RequestTest.php