commit 2624b909060e0967e16771de7a35261decd5a4a9
Author: Philippe Marschall <philippe.marschall@gmail.com>
Date:   Mon Apr 9 13:00:32 2012 +0200

    Avoid NPE in AutowiredAnnotationBeanPostProcessor

    Prior to this change, AABPP#determineRequiredStatus never checked the
    return value of ReflectionUtils#findMethod when searching for a
    '#required' attribute. This call returns null for annotations such as
    @Inject, @Value and @Resource, and subsequently causes a
    NullPointerException to be thrown when ReflectionUtils#invokeMethod is
    called. The NPE is caught immediately and #determineRequiredStatus
    returns defaulting to true, but this this approach is inefficient. It
    is also problematic for users who have set breakpoints on NPE -- they
    end up debugging into Spring internals, which is a false positive.

    This commit checks the return value of of ReflectionUtils#findMethod,
    and in the case of null, eagerly returns true.  There is no change to
    external behavior, simply a more efficient and debugging-friendly
    implementation.

    Existing test cases already cover this change, given that it is purely
    a refactoring.

    Issue: SPR-9316