commit 8e445f3a211395d43895f747d04158ff7f9c0913
Author: Phillip Webb <pwebb@vmware.com>
Date:   Wed Feb 20 09:38:43 2013 -0800

    Extend AnnotationMetadata and MethodMetadata

    Update AnnotationMetadata and MethodMetadata to extend from a new
    AnnotatedTypeMetadata base interface containing the methods that are
    common to both. Also introduce new getAllAnnotationAttributes methods
    providing MultiValueMap access to both annotation and meta-annotation
    attributes.

    Existing classreading and standard implementations have been
    refactored to support the new interface.