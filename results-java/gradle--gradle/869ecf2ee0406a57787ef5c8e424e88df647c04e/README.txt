commit 869ecf2ee0406a57787ef5c8e424e88df647c04e
Author: Rene Groeschke <rene@breskeby.com>
Date:   Wed Nov 26 15:14:15 2014 +0100

    refactor ScalaCompile, PlatformScalaCompile tasks

    - put common logic into AbstractScalaCompile
    - create Compiler for PlatformScalaCompile in ScalaToolProvider
    - no need for ScalaToolChain#getScalaClasspath + ScalaToolChain#getZincClasspath anymore
    - use hardcoded Scala version in 2.10.4
    +review REVIEW-5273