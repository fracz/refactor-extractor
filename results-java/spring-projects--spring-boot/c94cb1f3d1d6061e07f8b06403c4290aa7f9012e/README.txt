commit c94cb1f3d1d6061e07f8b06403c4290aa7f9012e
Author: Phillip Webb <pwebb@pivotal.io>
Date:   Wed Nov 4 23:41:52 2015 -0800

    Skip BeanInfo class search by default

    Set `CachedIntrospectionResults.IGNORE_BEANINFO_PROPERTY_NAME` by
    default to improve startup performance. The `spring.beaninfo.ignore`
    property can be set in `application.properties` if BeanInfo classes
    should be searched.

    Fixes gh-4390