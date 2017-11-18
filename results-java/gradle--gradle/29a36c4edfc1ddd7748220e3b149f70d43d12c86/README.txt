commit 29a36c4edfc1ddd7748220e3b149f70d43d12c86
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Dec 6 00:53:25 2011 +0100

    GRADLE-1933 Tooling api thread safety.

    Some of the stuff needs refactoring but I wanted to let it fly on the CI boxes to flush out potential problems. The commit touches the code in few places but that was necessary to make the integ test passing. Unfortunately, increasing the number of participating threads exposes some other issues so it's not the end of the concurrency story.

    -Added more selective strategy on the ConnectorServices/GradleLauncher regarding reusing instances. Before, all services were singletons but that's not the strategy we can afford if we go concurrent. At the moment only ToolingApiLoader is a singleton. It is implemented quite naively but refactoring will follow. Also, there memory consumption needs reviewing.
    -Enabled the integ test and added initial coverage for the outputs. They will be further improved to verify if concurrent builds / models are writing to the correct listeners. I've ran the test many times and it seems stable. The CI boxes will soon prove this statement true or false
    -Some changes to the DefaultConnection: I create an instance of embeddedLogging per every build request (if we run in forking mode). Previously, it was shared and it contributed to the concurrency issues. It can be refactored slightly to make it more clear. Removed redundant static call injectCustomFactory (it is already a part of the code flow - see the constructor of DefaultGradleLauncherFactory).