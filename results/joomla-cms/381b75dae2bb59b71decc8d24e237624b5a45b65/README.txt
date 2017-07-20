commit 381b75dae2bb59b71decc8d24e237624b5a45b65
Author: Roberto Segura <roberto@phproberto.com>
Date:   Wed Aug 31 21:57:26 2016 +0200

    Performance issues in sites with a lot of users groups (#11853)

    * [imp] create JHelperUsergroups class

    * [imp] increase performance of JHtml::_('user.groups')

    * [imp] improve performance of UsersModelGroups

    * [imp] JFormFieldUserGroupList::getOptions() performance improvements

    * [imp] JHtmlAccess::usergroups performance improvements

    * [imp] JHtmlAccess::usergroup performance improvements

    * [imp] UsersHelper::getGroups() performance improvements

    * [imp] JFormFieldGroupParent::getOptions() performance improvements

    * [imp] JHtmlUser::groups() performance improvements

    * [imp] JFormFieldRules::getUserGroups() performance improvements

    * Update groups.php

    * correct since tags

    * Update usergroups.php

    * Sorry i have implemented to much :)

    https://travis-ci.org/joomla/joomla-cms/jobs/156466958