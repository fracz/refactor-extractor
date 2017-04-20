commit 76efe3c0befc9051eaa4300413c3fa2839a0d74b
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Wed Jun 30 13:10:31 2010 -0400

    Zend\Controller\Action refactoring

    - Renamed Zend\Controller\Action\ActionInterface to
      Zend\Controller\ActionController
    - Renamed Zend\Controller\Action\Action to Zend\Controller\Action
    - Refactored tests and all dependent code to reflect this change