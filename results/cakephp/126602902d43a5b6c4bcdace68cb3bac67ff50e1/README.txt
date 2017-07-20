commit 126602902d43a5b6c4bcdace68cb3bac67ff50e1
Author: gwoo <gwoo@cakephp.org>
Date:   Mon Aug 25 22:55:10 2008 +0000

    fixes #5313, browser freeze with content length header on compressed assets. Some refactoring to improve speed, refactored Folder class to use read() inside of tree()

    git-svn-id: https://svn.cakephp.org/repo/branches/1.2.x.x@7502 3807eeeb-6ff5-0310-8944-8be069107fe0