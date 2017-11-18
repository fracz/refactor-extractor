commit 7ddc0b7a72aa66d699fecce3d855a6c70f844647
Author: Maksymilian Osowski <maxosowski@google.com>
Date:   Thu Jul 22 17:33:53 2010 +0100

    First stage of refactoring the code to handle crashes gracefully.

    There is a new activity (LayoutTestsExecuter) added that is responsible for acutally running the tests and sending the actual results to the new
    ManagerService class. This class will take over most of the functionality of the current LayoutTestsRunnerThread. At the moment
    LayoutTestsRunnerThread is changed so that after computing the tests' list it sends the list to LayoutTestsExecuter. The rest of the code is
    never called. It will be shifted to the service.

    Current implementation of ManagerService only prints the log message on receiving the bundle with actual results from LayoutTestsExecuter.

    Change-Id: I5adcbc20bb18ebf24324974bc66e4b31c4b81902