commit b90b92c7ac99f5f0cce1eaa14ac1cd598e2913f5
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Sat Apr 10 10:25:14 2010 -0400

    [NS4] Added Zend\Messenger

    - Will be used to support validation in Zend\Session
    - Will likely refactor Zend\Filter\FilterChain and/or
      Zend\Validator\ValidatorChain to utilize Messenger