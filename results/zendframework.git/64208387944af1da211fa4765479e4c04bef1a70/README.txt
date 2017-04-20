commit 64208387944af1da211fa4765479e4c04bef1a70
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Wed Aug 11 22:46:13 2010 -0400

    Minor improvements to Psr0Autoloader

    - Register Zend namespace by default
    - Ensure all namespaces have a trailing NS
    - Ensure all prefixes have a trailing underscore
    - Ensure all registered directories have a trailing directory separator