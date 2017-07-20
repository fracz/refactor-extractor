commit 7f11e8d7401e83c8f6bbc1e8c98dafae26b1a711
Author: epriestley <git@epriestley.com>
Date:   Tue Nov 12 14:37:04 2013 -0800

    Improve handling of email verification and "activated" accounts

    Summary:
    Small step forward which improves existing stuff or lays groudwork for future stuff:

      - Currently, to check for email verification, we have to single-query the email address on every page. Instead, denoramlize it into the user object.
        - Migrate all the existing users.
        - When the user verifies an email, mark them as `isEmailVerified` if the email is their primary email.
        - Just make the checks look at the `isEmailVerified` field.
      - Add a new check, `isUserActivated()`, to cover email-verified plus disabled. Currently, a non-verified-but-not-disabled user could theoretically use Conduit over SSH, if anyone deployed it. Tighten that up.
      - Add an `isApproved` flag, which is always true for now. In a future diff, I want to add a default-on admin approval queue for new accounts, to prevent configuration mistakes. The way it will work is:
        - When the queue is enabled, registering users are created with `isApproved = false`.
        - Admins are sent an email, "[Phabricator] New User Approval (alincoln)", telling them that a new user is waiting for approval.
        - They go to the web UI and approve the user.
        - Manually-created accounts are auto-approved.
        - The email will have instructions for disabling the queue.

    I think this queue will be helpful for new installs and give them peace of mind, and when you go to disable it we have a better opportunity to warn you about exactly what that means.

    Generally, I want to improve the default safety of registration, since if you just blindly coast through the path of least resistance right now your install ends up pretty open, and realistically few installs are on VPNs.

    Test Plan:
      - Ran migration, verified `isEmailVerified` populated correctly.
      - Created a new user, checked DB for verified (not verified).
      - Verified, checked DB (now verified).
      - Used Conduit, People, Diffusion.

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: chad, aran

    Differential Revision: https://secure.phabricator.com/D7572