commit d5f587b1c6cb7da9f7b9d4bb7533ad8efb443a9d
Author: Misagh Moayyed <mmoayyed@unicon.net>
Date:   Mon Jun 10 11:00:33 2013 -0700

    Ldap credential resolver changes that delegates to an inner resolver for constructing principal out of the request.

    Added minor improvements to ease the definition of search queries based on uid attr providers of person directory.