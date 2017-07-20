commit a95c2a68aaededb5538da3df4d40c88879c4c45d
Author: Dries Buytaert <dries@buytaert.net>
Date:   Sun Dec 30 16:16:38 2001 +0000

    - import.module:

        + Improved input filtering; this should make the news items look
          more consistent in terms of mark-up.

        + Quoted all array indices: converted all instances of $foo[bar]
          to $foo["bar"].  Made various other changes to make the import
          module compliant with the coding style.

    - theme.inc:

        + Fixed small XHTML glitch

    - comment system:

        + Made it possible for users to edit their comments (when certain
          criteria are matched).

        + Renamed the SQL table field "lid" to "nid" and updated the code
          to reflect this change: this is a rather /annoying/ change that
          has been asked for a few times.  It will impact the contributed
          BBS/forum modules and requires a tiny SQL update:

            sql> ALTER TABLE comments CHANGE lid nid int(10) NOT NULL;

        + Moved most (all?) of the comment related logic from node.php to
          comment.module where it belongs.  This also marks a first step
          towards removing/reducing "node.php".

        + Added a delete button to the comment admin form and made it so
          that Drupal prompts for confirmation prior to deleting a comment
          from the database.  This behavior is similar to that of deleting
          nodes.

        + Disabled comment moderation for now.

        + Some of the above changes will make it easier to integrate the
          upcomcing mail-to-web and web-to-mail gateways.  They are part
          of a bigger plan.  ;)

    - node system:

        + Made it so that updating nodes (like for instance updating blog
          entries) won't trigger the submission rate throttle.

        + Fixed a small glitch where a node's title wasn't always passed
          to the $theme->header() function.

        + Made "node_array()" and "node_object()" more generic and named
          them "object2array()" and "array2object()".

        + Moved most (all?) of the comment related logic from node.php to
          comment.module where it belongs.  This also marks a first step
          towards removing/reducing "node.php".

    - misc:

        + Applied three patches by Foxen.  One to improve performance of
          the book module, and two other patches to fix small glitches in
          common.inc.  Thanks Foxen!