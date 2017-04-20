commit de5ea5fc123126b8c772747ad9b4e50192dbd679
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Sat Jul 3 23:33:13 2010 -0400

    Zend\Search\Lucene cleanup

    - Renamed\reorganized all "Component\Component" instances (i.e., duplicate
      names)
    - Renamed all interfaces ending in "Interface"
    - Fixed a few cases where abstract naming was incorrect
    - Updated all code and tests to reflect above changes