commit 293a475e398b132032f44cee585e06e25327a0a5
Author: epriestley <git@epriestley.com>
Date:   Wed Jul 10 15:17:38 2013 -0700

    Show why recipients were excluded from mail

    Summary:
    Ref T3306. This interface has a hard time balancing security/policy issues and I'm not sure what the best way forward is. Some possibilities:

      # We just let you see everything from the web UI.
        - This makes debugging easier.
        - Anyone who can see this stuff can trivially take over any user's account with five seconds of work and no technical expertise (reset their password from the web UI, then go read the email and click the link).
      # We let you see everything, but only for messages you were a recipient of or author of.
        - This makes it much more difficult to debug issues with mailing lists.
          - But maybe we could just say mailing list recipients are "public", or define some other ruleset.
        - Generally this gets privacy and ease of use right.
      # We could move the whole thing to the CLI.
        - Makes the UI/UX way worse.
      # We could strike an awkward balance between concerns, as we do now.
        - We expose //who// sent and received messages, but not the content of the messages. This doesn't feel great.

    I'm inclined to probably go with (2) and figure something out for mailing lists?

    Anyway, irrespective of that this should generally make things more clear, and improves the code a lot if nothing else.

    Test Plan:
    {F49546}

      - Looked at a bunch of mail.
      - Sent mail from different apps.
      - Checked that recipients seem correct.

    Reviewers: btrahan, chad

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T3306

    Differential Revision: https://secure.phabricator.com/D6413