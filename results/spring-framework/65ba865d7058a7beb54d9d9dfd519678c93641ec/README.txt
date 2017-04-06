commit 65ba865d7058a7beb54d9d9dfd519678c93641ec
Author: Juergen Hoeller <jhoeller@pivotal.io>
Date:   Fri Mar 24 12:15:45 2017 +0100

    Support for populating model attributes through data class constructors

    Includes a new overloaded ModelAndView constructor with an HttpStatus argument, as well as a HandlerMethodArgumentResolverSupport refactoring (revised checkParameterType signature, actually implementing the HandlerMethodArgumentResolver interface).

    Issue: SPR-15199