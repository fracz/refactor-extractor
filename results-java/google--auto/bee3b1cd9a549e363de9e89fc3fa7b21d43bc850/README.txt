commit bee3b1cd9a549e363de9e89fc3fa7b21d43bc850
Author: dpb <dpb@google.com>
Date:   Wed Jul 8 07:03:47 2015 -0700

    Name factories for nested classes using the enclosing class names, not just the annotated class name. For example, the factory for Foo.Bar is named Foo_BarFactory instead of just BarFactory.

    Includes a small refactoring of AutoFactoryDeclaration.getFactoryName(). The factory class name is always based on the target type name, which is known when AutoFactoryDeclaration is instantiated. So getFactoryName() doesn't need any parameters.

    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=97771117