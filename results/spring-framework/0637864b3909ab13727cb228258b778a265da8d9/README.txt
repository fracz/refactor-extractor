commit 0637864b3909ab13727cb228258b778a265da8d9
Author: Sam Brannen <sam@sambrannen.com>
Date:   Wed Feb 26 17:51:26 2014 +0100

    Ensure AnnotationUtils is compatible with Java 6

    The previous commit introduced a dependency on
    Class.getDeclaredAnnotation() which is a Java 8 API.

    This commit refactors AnnotationUtils.findAnnotation(Class, Class, Set)
    to use Class.getAnnotation() in conjunction with
    isAnnotationDeclaredLocally() in order to achieve the same desired
    behavior.

    Issue: SPR-11475