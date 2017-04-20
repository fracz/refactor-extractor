commit a823f613d0f21964693d0c9739db0e7b7aa3999a
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Tue Nov 15 11:44:35 2011 -0600

    Refactored Twitter client

    - Moved Zend\Service\Twitter class into Zend\Service\Twitter namespace
    - Refactored Twitter component to use new HTTP API properly
    - Fixed issues in OAuth client stemming from incomplete refactoring
      towards new HTTP API
    - Updated test suite stub generation of Twitter client to call
      appropriate methods
    - Moved Nirvanic client under it's own component directory