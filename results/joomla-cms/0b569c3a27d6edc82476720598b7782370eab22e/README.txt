commit 0b569c3a27d6edc82476720598b7782370eab22e
Author: andrepereiradasilva <andrepereiradasilva@users.noreply.github.com>
Date:   Thu Jul 21 22:26:26 2016 +0100

    [update sites rebuild/delete] Only for non core extensions. Solves #10826 (#10828)

    * only allow to delete/update update sites whose extension id is higher than 1000

    * cs

    * use the core extension element instead

    * remove forgotten print_r

    * better core component detection

    * minor code improvements

    * do not allow to rebuild update sites with joomla extension plugin disabled

    * cs

    * remvoe unneed try captch