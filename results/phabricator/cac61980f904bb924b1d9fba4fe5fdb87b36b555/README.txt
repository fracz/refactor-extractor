commit cac61980f904bb924b1d9fba4fe5fdb87b36b555
Author: epriestley <git@epriestley.com>
Date:   Tue May 20 11:43:45 2014 -0700

    Add "temporary tokens" to auth, for SMS codes, TOTP codes, reset codes, etc

    Summary:
    Ref T4398. We have several auth-related systems which require (or are improved by) the ability to hand out one-time codes which expire after a short period of time.

    In particular, these are:

      - SMS multi-factor: we need to be able to hand out one-time codes for this in order to prove the user has the phone.
      - Password reset emails: we use a time-based rotating token right now, but we could improve this with a one-time token, so once you reset your password the link is dead.
      - TOTP auth: we don't need to verify/invalidate keys, but can improve security by doing so.

    This adds a generic one-time code storage table, and strengthens the TOTP enrollment process by using it. Specifically, you can no longer edit the enrollment form (the one with a QR code) to force your own key as the TOTP key: only keys Phabricator generated are accepted. This has no practical security impact, but generally helps raise the barrier potential attackers face.

    Followup changes will use this for reset emails, then implement SMS multi-factor.

    Test Plan:
      - Enrolled in TOTP multi-factor auth.
      - Submitted an error in the form, saw the same key presented.
      - Edited the form with web tools to provide a different key, saw it reject and the server generate an alternate.
      - Change the expiration to 5 seconds instead of 1 hour, submitted the form over and over again, saw it cycle the key after 5 seconds.
      - Looked at the database and saw the tokens I expected.
      - Ran the GC and saw all the 5-second expiry tokens get cleaned up.

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Maniphest Tasks: T4398

    Differential Revision: https://secure.phabricator.com/D9217