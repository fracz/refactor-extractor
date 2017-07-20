commit b056c5abbfb3cf973d09ebcbdd43ea9fe4131b1b
Author: gwoo <gwoo@cakephp.org>
Date:   Mon Nov 27 05:23:33 2006 +0000

    refactoring scaffold, if var $uses exists scaffold will use the first model specified in the array, if $scaffold is an array, only methods added to the array will allow scaffold to render, ex:  var $scaffold = array('index'); only the index action will be rendered

    git-svn-id: https://svn.cakephp.org/repo/branches/1.2.x.x@3988 3807eeeb-6ff5-0310-8944-8be069107fe0