commit f8ed6422f8e2492ca80985a5d49ac375eaafb3f9
Author: epriestley <git@epriestley.com>
Date:   Mon Jun 24 15:56:01 2013 -0700

    Provide an auto-refresh mechanism for OAuth providers to deliver fresh tokens

    Summary:
    Ref T2852. Give OAuth providers a formal method so you can ask them for tokens; they issue a refresh request if necessary.

    We could automatically refresh these tokens in daemons as they near expiry to improve performance; refreshes are blocking in-process round trip requests. If we do this for all tokens, it's a lot of requests (say, 20k users * 2 auth mechanisms * 1-hour tokens ~= a million requests a day). We could do it selectively for tokens that are actually in use (i.e., if we refresh a token in response to a user request, we keep refreshing it for 24 hours automatically). For now, I'm not pursuing any of this.

    If we fail to refresh a token, we don't have a great way to communicate it to the user right now. The remedy is "log out and log in again", but there's no way for them to figure this out. The major issue is that a lot of OAuth integrations should not throw if they fail, or can't reasonably be rasied to the user (e.g., activity in daemons, loading profile pictures, enriching links, etc). For now, this shouldn't really happen. In future diffs, I plan to make the "External Accounts" settings page provide some information about tokens again, and possibly push some flag to accounts like "you should refresh your X link", but we'll see if issues crop up.

    Test Plan: Used `bin/auth refresh` to verify refreshes. I'll wait an hour and reload a page with an Asana link to verify the auto-refresh part.

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T2852

    Differential Revision: https://secure.phabricator.com/D6280