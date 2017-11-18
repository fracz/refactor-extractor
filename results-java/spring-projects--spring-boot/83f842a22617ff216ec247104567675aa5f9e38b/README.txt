commit 83f842a22617ff216ec247104567675aa5f9e38b
Author: Andy Wilkinson <awilkinson@pivotal.io>
Date:   Wed Apr 8 10:44:50 2015 +0100

    Reinstate support for relaxed binding for endpoint enablement

    This commit improves upon the changes made in a8bf9d3 by adding
    support for relaxed binding of the endpoints.enabled and
    endpoints.<name>.enabled properties. This is achieved by replacing
    use of @ConditionalOnExpression (which does not support relaxed
    binding) with a custom condition implementation that uses
    RelaxedPropertyResolver.

    Closes gh-2767