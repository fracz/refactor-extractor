commit e5a253e68951dcf975b9844b3b39afac2222b503
Author: Andy Wilkinson <awilkinson@pivotal.io>
Date:   Thu Apr 7 12:14:22 2016 +0100

    Improve diagnostics when OnBeanCondition type deduction fails

    When @ConditionalOnBean or @ConditionalOnMissingBean are used on a
    @Bean method, they will, in the absence of any other configuration,
    attempt to deduce the bean's type by examining the method's return
    type. This deduction can fail. See gh-4841, gh-4934, and gh-5624
    for some examples of possible failure causes. Previously, this
    failure was only logged as a debug message leaving the user with a
    misleading message suggesting that the @ConditionalOnBean or
    @ConditionalOnMissingBean annotation was not configured correctly.

    This commit improves the diagnostics by mention the possibility of
    type deduction in the exception message and including the exception
    that caused deduction to fail as the cause.

    Closes gh-4934