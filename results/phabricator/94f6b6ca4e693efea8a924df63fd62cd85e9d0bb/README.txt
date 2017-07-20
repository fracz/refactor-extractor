commit 94f6b6ca4e693efea8a924df63fd62cd85e9d0bb
Author: epriestley <git@epriestley.com>
Date:   Mon Feb 4 17:06:34 2013 -0800

    Fix every HTML issue I could find

    Summary:
    I attempted to test every interface. I probably missed some stuff, but I at least have some level of confidence that the `phutil_tag` branch is fairly stable.

    Fixed these issues:

    [1] Fixed a Herald issue with object links in transcripts. Some actions return
    links; this was previously hard-coded.
    [2] DarkConsole refactoring created an issue where the "`" event handler registered too many times.
    [3] Fixed a bug where `strlen($value)` was being checked, but fields may now return array(). Possibly we should implement phutil_is_empty_html() or similar.
    [4] Fixed a undefined variable issue for image edit transactions.
    [5] Fixed an issue with rendering participant transactions. This required phutil_safe_html() because `pht()` can't handle `array()` for `%s`.
    [6] Fixed an issue where feed was entirely overescaped by reverting an overly ambitious render_tag -> tag.
    [7] Fixed an issue with strict tables and inserting `''` instead of `0` into an integer column.
    [8] Fixed an issue where &bull; was shown escaped.
    [9] Fixed an issue where "no data" was overescaped.
    [10] Fixed an issue with strict tables and inserting `''` instead of `0` into an integer column.
    [11] Fixed an issue with strict tables and inserting `''`.
    [12] Fixed an issue with missing space after ":" for mini panels.

    Encountered (but did not fix) these issues:

    [X1] "e" works incorrectly on comments you are not allowed to edit. Did not fix.
    [X2] Coverage currently interacts incorrectly with "--everything" for Phutil tests.

    Test Plan:
      - Viewed Differential.
      - Created a diff via copy/paste.
      - Viewed standalone diff.
      - Jumped to diff via changeset table.
      - Created a revision.
      - Updated revision.
      - Added a comment.
      - Edited revision dependencies.
      - Edited revision tasks.
      - Viewed MetaMTA transcripts.
      - Viewed Herald transcripts [1].
      - Downloaded raw diff.
      - Flagged / unflagged revision.
      - Added/edited/deleted inline comment.
      - Collapsed/expanded file.
      - Did show raw left.
      - Did show raw right.
      - Checked previews for available actions.
      - Clicked remarkup buttons
      - Used filetree view.
      - Used keyboard: F, j, k, J, K, n, p, t, h, "?" [2] [X1].
      - Created a meme.
      - Uploaded a file via drag and drop.
      - Viewed a revision with no reviewers.
      - Viewed a revision with >100 files.
      - Viewed various other revisions [3].
      - Viewed an image diff.
      - Added image diff inline comments.
      - Viewed Maniphest.
      - Ran various queries.
      - Created task.
      - Created similar task.
      - Added comments to tasks.
      - Ran custom query.
      - Saved custom query.
      - Edited custom queries.
      - Drag-reordered tasks.
      - Batch edited tasks.
      - Exported tasks to excel.
      - Looked at reports (issue in T2311 notwithstanding).
      - Viewed Diffusion.
      - Browsed Git, SVN, HG repositories.
      - Looked at history, browse, change, commit views.
      - Viewed audit.
      - Performed various audit searches.
      - Viewed Paste.
      - Performed paste searches.
      - Created, edited, forked paste.
      - Viewed Phriction.
      - Edited a page.
      - Viewed edit history.
      - Used search typeahead to search for user / application.
      - Used search to search for text.
      - Viewed Phame.
      - Viewed Blog, Post.
      - Viewed live post.
      - Published/unpublished post.
      - Previewed post.
      - Viewed Pholio.
      - Edited/commented mock.
      - Viewed ponder.
      - Viewed question.
      - Added answer/comment.
      - Viewed Diviner.
      - Viewed Conpherence [4] [5].
      - Made Conpherence updates.
      - Viewed calendar.
      - Created status.
      - Viewed status.
      - Viewed Feed [6].
      - Viewed Projects.
      - Viewed project detail.
      - Edited project.
      - Viewed Owners.
      - Viewed package detail.
      - Edited package [7].
      - Viewed flags.
      - Edited flag.
      - Deleted flag.
      - Viewed Herald.
      - Viewed rules.
      - Created rule.
      - Edited rule.
      - Viewed edit log.
      - Viewed transcripts.
      - Inspected a transcript.
      - Viewed People.
      - Viewed list.
      - Administrated user.
      - Checked username/delete stuff.
      - Looked at create/import LDAP/activity logs.
      - Looked at a user profile.
      - Looked at user about page.
      - Looked at Repositories.
      - Edited repository.
      - Edited arcanist project.
      - Looked at daemons.
      - Looked at all daemons [8].
      - Viewed combined log.
      - Looked at configuration.
      - Edited configuration.
      - Looked at setup issues [9].
      - Looked at current settings.
      - Looked at application list.
      - Installed / uninstalled applications [10].
      - Looked at mailing lists.
      - Created a mailing list.
      - Edited a mailing list.
      - Looked at sent mail.
      - Looked at received mail.
      - Looked at send/receive tests.
      - Looked at settings.
      - Clicked through all the panels.
      - Looked at slowvote.
      - Created a slowvote [11].
      - Voted in a slowvote.
      - Looked at Macro.
      - Created a macro.
      - Edited a macro.
      - Commented on a macro.
      - Looked at Countdown.
      - Created a Countdown.
      - Looked at it.
      - Looked at Drydock.
      - Poked around a bit.
      - Looked at Fact.
      - Poked around a bit.
      - Looked at files.
      - Looked at a file.
      - Uploaded a file.
      - Looked at Conduit.
      - Made a Conduit call.
      - Looked at UIExamples.
      - Looked at PHPAST.
      - Looked at PHIDs.
      - Looked at notification menu.
      - Looked at notification detail.
      - Logged out.
      - Logged in.
      - Looked at homepage [12].
      - Ran `arc unit --everything --no-coverage` [X2].

    Reviewers: vrana, btrahan

    Reviewed By: vrana

    CC: aran

    Maniphest Tasks: T2432

    Differential Revision: https://secure.phabricator.com/D4807