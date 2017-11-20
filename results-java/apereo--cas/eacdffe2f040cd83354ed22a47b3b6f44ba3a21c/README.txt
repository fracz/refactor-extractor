commit eacdffe2f040cd83354ed22a47b3b6f44ba3a21c
Author: Marvin S. Addison <marvin.addison@gmail.com>
Date:   Thu Jun 6 08:45:31 2013 -0400

    CAS-1238 Credential refactoring.

    1. Rename Credentials -> Credential for clarity w/r/t MFA.
    2. Move Credential up to authentication package since it is a core
       authentication component.
    3. Add Credential#getId() so that credentials that are safe for long-term
       storage can simply implement both Credential and CredentialMetaData and
       consequent be stored directly in HandlerResult.