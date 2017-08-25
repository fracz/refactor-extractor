commit 83c7ebbc7cbe9678ecd53823e5cb5986c83093d6
Author: Christian Morgan <git@caponica.com>
Date:   Thu Aug 8 12:23:16 2013 +0100

    Updated console command docs and tweaked GenerateAdminCommand:
    * expanded sonata:admin:generate docs
    * switched order of list and explain in docs, since you need to list first before you know what to explain
    * refactored GenerateAdminCommand::getAdminServiceId() so it only strips the end off Bundle names that end in "Bundle"
    * edit for readability