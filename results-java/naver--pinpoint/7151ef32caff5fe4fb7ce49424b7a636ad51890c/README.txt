commit 7151ef32caff5fe4fb7ce49424b7a636ad51890c
Author: Woonduk Kang <wd.kang@navercorp.com>
Date:   Thu Feb 12 20:52:49 2015 +0900

    #177 remove dependency of AnnotationKeyMatcher on ServiceType
     - decoupling AnnotationKeyMatcher
     -refactoring plugin system
     -reduce singleton pattern
       AnnotationKeyMatcher