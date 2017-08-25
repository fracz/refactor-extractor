commit 1d57f8622c85bc3126153edbb98c046bd099b581
Author: Petr Skoda <commits@skodak.org>
Date:   Tue Apr 5 18:33:35 2011 +0200

    revert MDL-26577 added session cleaning to groups_get_activity_group() that fixes an issue with dirty session data. refactored session cleaning into one function, added caching to reduce queries.

    (reverse-merged from commit 0d1e49d46838f6916f93561866fdd83beaf24a0f) This change was causing fatal errors on the course level, I am going to fix it myself.