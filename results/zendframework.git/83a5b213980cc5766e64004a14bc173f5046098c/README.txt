commit 83a5b213980cc5766e64004a14bc173f5046098c
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Fri Jun 29 08:59:04 2012 -0500

    [zen-60] Refactor to provide better usability

    - Created ParserInterface
      - defines onCreateAnnotation(), registerAnnotation(), and
        registerAnnotations()
      - refactored GenericAnnotationParser and DoctrineAnnotationParser to
        follow above interface; removed constructor from
        GenericAnnotationParser, and made registerAnnotation accept string
        class names.
    - Added attach() method to AnnotationManager; allows attaching a parser
      instance to the createAnnotation event.