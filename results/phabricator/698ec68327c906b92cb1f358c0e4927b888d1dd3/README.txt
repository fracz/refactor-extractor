commit 698ec68327c906b92cb1f358c0e4927b888d1dd3
Author: epriestley <git@epriestley.com>
Date:   Fri Mar 30 10:49:55 2012 -0700

    General Herald refactoring pass

    Summary:
    **Who can delete global rules?**: I discussed this with @jungejason. The current behavior is that the rule author or any administrator can delete a global rule, but this
    isn't consistent with who can edit a rule (anyone) and doesn't really make much sense (it's an artifact of the global/personal split). I proposed that anyone can delete a
    rule but we don't actually delete them, and log the deletion. However, when it came time to actually write the code for this I backed off a bit and continued actually
    deleting the rules -- I think this does a reasonable job of balancing accountability with complexity. So the new impelmentation is:

      - Personal rules can be deleted only by their owners.
      - Global rules can be deleted by any user.
      - All deletes are logged.
      - Logs are more detailed.
      - All logged actions can be viewed in aggregate.

    **Minor Cleanup**

      - Merged `HomeController` and `AllController`.
      - Moved most queries to Query classes.
      - Use AphrontFormSelectControl::renderSelectTag() where appropriate (this is a fairly recent addition).
      - Use an AphrontErrorView to render the dry run notice (this didn't exist when I ported).
      - Reenable some transaction code (this works again now).
      - Removed the ability for admins to change rule authors (this was a little buggy, messy, and doesn't make tons of sense after the personal/global rule split).
      - Rules which depend on other rules now display the right options (all global rules, all your personal rules for personal rules).
      - Fix a bug in AphrontTableView where the "no data" cell would be rendered too wide if some columns are not visible.
      - Allow selectFilter() in AphrontNavFilterView to be called without a 'default' argument.

    Test Plan:
      - Browsed, created, edited, deleted personal and gules.
      - Verified generated logs.
      - Did some dry runs.
      - Verified transcript list and transcript details.
      - Created/edited all/any rules; created/edited once/every time rules.
      - Filtered admin views by users.

    Reviewers: jungejason, btrahan

    Reviewed By: btrahan

    CC: aran, epriestley

    Differential Revision: https://secure.phabricator.com/D2040