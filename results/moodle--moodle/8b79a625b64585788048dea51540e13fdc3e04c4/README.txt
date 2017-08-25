commit 8b79a625b64585788048dea51540e13fdc3e04c4
Author: skodak <skodak>
Date:   Sat Apr 19 10:49:53 2008 +0000

    MDL-13936 forum reply refactoring and bugfixing - the forum_user_can_post() did not have discussion parameter which was a problem because the login depends on discussion group - there was a hack in discussion.php and view.php working around this, but it was not present in cron; sorry for the change of API, but it was required; merged from MOODLE_19_STABLE