commit d0ff887e8a93b87676642c4fac831b99f95b4d98
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Fri May 25 20:09:56 2012 +0200

    improvements to Groovy compiler tests

    - added 2.0.0-beta-3 to the list of Groovy versions to be tested
    - moved one test from ApiGroovyCompilerIntegrationSpec to GroovyCompilerIntegrationSpec
    - deleted ApiGroovyCompilerIntegrationSpec (InProcessGroovyCompilerIntegrationTest now covers everything the former did)