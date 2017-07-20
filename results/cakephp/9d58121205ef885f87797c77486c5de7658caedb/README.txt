commit 9d58121205ef885f87797c77486c5de7658caedb
Author: nate <nate@cakephp.org>
Date:   Thu Dec 13 07:03:59 2007 +0000

    Removing trailing slash from normalized URLs in AuthComponent, moving AuthComponent::_normalizeURL() to Router::normalize(), refactoring (Ticket #3042)


    git-svn-id: https://svn.cakephp.org/repo/branches/1.2.x.x@6145 3807eeeb-6ff5-0310-8944-8be069107fe0