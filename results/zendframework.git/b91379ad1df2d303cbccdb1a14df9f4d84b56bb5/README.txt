commit b91379ad1df2d303cbccdb1a14df9f4d84b56bb5
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Fri Jun 29 10:05:54 2012 -0500

    [zen-60] Testing for annotation refactoring

    - Added tests for GenericAnnotationParser and DoctrineAnnotationParser
    - Modified AnnotationManager tests to test a mixed annotation situation
    - Discovered an issue with DoctrineAnnotationParser; needed to provide a
      loader to AnnotationRegistry that attempts to autoload an annotation
      class in order for the functionality to work with namespaced
      annotation classes.