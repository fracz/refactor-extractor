commit e71f583c782a98e977d6a5c450dff856b132e5c6
Author: Michal Karpinski <mkarpinski@google.com>
Date:   Fri Jan 13 18:18:49 2017 +0000

    Strong auth timeout refactor

    Move timeout scheduling mechanism from KeyguardUpdateMonitor to
    LockSettingsStrongAuth.
    Move reporting about successful strong auth unlock from
    KeyguardUpdateMonitor#reportSuccessfulStrongAuthUnlockAttempt()
    to LockSettingsService#doVerifyCredential() - the latter also
    covers work challenge strong auth unlocking.

    Test: manual with all types of strong and non-strong auth, including work challenge
    Bug: 29825955
    Change-Id: I38e51b21e3a455b95e3c857e091fe07ee388c7f8