commit fbd85925de74ea02dbf3dbe521743097e6b3356b
Author: Sebastien Deleuze <sdeleuze@pivotal.io>
Date:   Wed Dec 3 09:49:41 2014 +0100

    Use Jackson improved default configuration everywhere

    With this commit, Jackson builder is now used in spring-websocket
    to create the ObjectMapper instance.

    It is not possible to use the builder for spring-messaging
    and spring-jms since these modules don't have a dependency on
    spring-web, thus they now just customize the same features:
     - MapperFeature#DEFAULT_VIEW_INCLUSION is disabled
     - DeserializationFeature#FAIL_ON_UNKNOWN_PROPERTIES is disabled

    Issue: SPR-12293