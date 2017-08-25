commit d728b40c8892344ead04cc97956c7fcb4727dc07
Author: David Grudl <david@grudl.com>
Date:   Mon Jul 14 23:57:19 2008 +0000

    - Web::Uri separated into Uri & UriScript
    - Web::Uri returns authority part without user:pass for http & https schemes
    - modified IPresenter interface (PresenterRequest is passed in constructor)
    - Nette::Debug profiler improved