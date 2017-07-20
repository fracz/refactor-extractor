commit bc718882491cfe52276ac7fe97b50bf1c27a671b
Author: epriestley <git@epriestley.com>
Date:   Sat May 28 07:17:42 2011 -0700

    Mask typed passwords as they are entered into 'accountadmin'

    Summary:
    Currently, we echo the password as the user types it. This turns out to be a bit
    of an issue in over-the-shoulder installs. Instead, disable tty echo while the
    user is typing their password so nothing is shown (like how 'sudo' works).

    Also show a better error message if the user chooses a duplicate email; without
    testing for this we just throw a duplicate key exception when saving, which
    isn't easy to understand. The other duplicate key exception is duplicate
    username, which is impossible (the script updates rather than creating in this
    case).

    There's currently a bug where creating a user and setting their password at the
    same time doesn't work. This is because we hash the PHID into the password hash,
    but it's empty if the user hasn't been persisted yet. Make sure the user is
    persisted before setting their password.

    Finally, fix an issue where $original would have the new username set, creating
    a somewhat confusing summary at the end.

    I'm also going to improve the password behavior/explanation here once I add
    welcome emails ("Hi Joe, epriestley created an account for you on Phabricator,
    click here to login...").

    Test Plan:
    - Typed a password and didn't have it echoed. I also tested this on Ubuntu
    without encountering problems.
      - Chose a duplicate email, got a useful error message instead of the exception
    I'd encountered earlier.
      - Created a new user with a password in one pass and logged in as that user,
    this worked properly.
      - Verified summary table does not contain username for new users.

    Reviewed By: jungejason
    Reviewers: jungejason, tuomaspelkonen, aran
    CC: moskov, jr, aran, jungejason
    Differential Revision: 358