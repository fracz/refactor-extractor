commit db1f98b0709571cdeb9e59d65e386f5fd1a4e88e
Author: David Grudl <david@grudl.com>
Date:   Tue Oct 21 21:49:58 2008 +0000

    - consolidated parameter $need (everywhere is TRUE by default!)
    - added MultiSelectBox, separated from SelectBox
    - added Form::setMethod(), removed Form::setAction()'s second parameter
    - reverted PresenterRequest::isPost()
    - new const NETTE_DIR
    - improved RobotLoader autoRebuild mode
    - Nette directory is not scanned by RobotLoader by default (must be added manually)
    - added HttpRequest::getRemoteHost()