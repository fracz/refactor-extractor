commit 19d245b792e1d941fbba9b04ae20ce9d6e6e18f8
Author: Irfan Sheriff <isheriff@google.com>
Date:   Thu Nov 11 16:40:06 2010 -0800

    Split SupplicantStateTracker and refactor

    - Move SupplicantStateTracker into a seperate file. In the
    process clean up code that needs to be present only in SupplicantStateTracker

    - Retry twice for password failures

    - Clean up supplicant state handling. Dont treat supplicant states
    as HSM what values. Instead, just retain SUPPLICANT_STATE_CHANGE

    - Add event logging in SupplicantStateTracker

    - Move scan handling out SupplicantStateTracker

    Change-Id: I083e1c40ef5a858164493b1440b82c5751b3bfb5