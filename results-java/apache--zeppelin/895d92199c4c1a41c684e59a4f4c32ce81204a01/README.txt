commit 895d92199c4c1a41c684e59a4f4c32ce81204a01
Author: Jongyoul Lee <jongyoul@gmail.com>
Date:   Sun Dec 25 03:59:04 2016 +0900

    [MINOR] Refactoring Job and Paragraph

    ### What is this PR for?
    Job class has only two sub classes. I don't think we need Job abstraction class anymore, then I'll move all function of Job into Paragraph and InterpretJob. Paragraph is used into zeppelin-server and InterpretJob is done by remoteInterpreterServer. I think what we disconnect those two different classes is better to maintain each side of codes. This PR moves two variable - result and results - from Job to Paragraph in order to remove Job class. It also based on ZEPPELIN-1594(#1753), thus all reviewers click the last commit and review it.

    ### What type of PR is it?
    [Refactoring]

    ### Todos
    * [x] - Moved these variables into Paragraph

    ### What is the Jira issue?
    N/A

    ### How should this be tested?
    All things are same as before

    ### Screenshots (if appropriate)
    N/A

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Jongyoul Lee <jongyoul@gmail.com>

    Closes #1793 from jongyoul/minor/refactoring-job-paragraph and squashes the following commits:

    ca0be86 [Jongyoul Lee] Fixed some weird indentation
    2fe9b63 [Jongyoul Lee] Reformat code
    8de238b [Jongyoul Lee] Changed wrong variable name
    6923002 [Jongyoul Lee] Changed to store result from jobRun() in Job
    54a2423 [Jongyoul Lee] Moved result(s) from Job into Paragraph