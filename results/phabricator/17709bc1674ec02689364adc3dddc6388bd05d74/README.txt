commit 17709bc1674ec02689364adc3dddc6388bd05d74
Author: epriestley <git@epriestley.com>
Date:   Mon Apr 28 09:27:11 2014 -0700

    Add multi-factor auth and TOTP support

    Summary:
    Ref T4398. This is still pretty rough and isn't exposed in the UI yet, but basically works. Some missing features / areas for improvement:

      - Rate limiting attempts (see TODO).
      - Marking tokens used after they're used once (see TODO), maybe. I can't think of ways an attacker could capture a token without also capturing a session, offhand.
      - Actually turning this on (see TODO).
      - This workflow is pretty wordy. It would be nice to calm it down a bit.
      - But also add more help/context to help users figure out what's going on here, I think it's not very obvious if you don't already know what "TOTP" is.
      - Add admin tool to strip auth factors off an account ("Help, I lost my phone and can't log in!").
      - Add admin tool to show users who don't have multi-factor auth? (so you can pester them)
      - Generate QR codes to make the transfer process easier (they're fairly complicated).
      - Make the "entering hi-sec" workflow actually check for auth factors and use them correctly.
      - Turn this on so users can use it.
      - Adding SMS as an option would be nice eventually.
      - Adding "password" as an option, maybe? TOTP feels fairly good to me.

    I'll post a couple of screens...

    Test Plan:
      - Added TOTP token with Google Authenticator.
      - Added TOTP token with Authy.

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Maniphest Tasks: T4398

    Differential Revision: https://secure.phabricator.com/D8875