commit d6fd27477174ba0f24479fc4117cac2ec5f935ca
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Mon Aug 24 16:33:22 2015 -0400

    Add HttpServer and Jetty/Tomcat/RxNetty implementations

    This is a refactoring of the existing "echo" integration test with the
    goal to make it easier to add further integration tests.

    The HttpServer abstraction is on the test source side for now.