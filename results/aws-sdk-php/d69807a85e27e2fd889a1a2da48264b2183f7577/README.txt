commit d69807a85e27e2fd889a1a2da48264b2183f7577
Author: Michael Dowling <mtdowling@gmail.com>
Date:   Wed Nov 26 15:02:50 2014 -0800

    Adding credential providers and refactoring creds.

    This commit adds a concept of credential providers, or functions that
    when called return credentials or return null. This commit introduces
    several credential providers that allow you to compose conditional
    credential loading logic based on the environment the code is run in.

    Addresses #371