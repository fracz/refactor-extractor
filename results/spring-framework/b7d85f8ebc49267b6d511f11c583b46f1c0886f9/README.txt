commit b7d85f8ebc49267b6d511f11c583b46f1c0886f9
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Fri Oct 21 15:02:27 2016 +0200

    Expose ResolvableType in NoSuchBeanDefinitionException

    This commit improves NoSuchBeanDefinitionException to expose a full
    ResolvableType rather than a raw class if a lookup by type failed. This
    allows to know more about the underlying type and is typically useful
    when a collection or map is required as the relevant generic type is the
    actual bean that wasn't found.

    Issue: SPR-14831