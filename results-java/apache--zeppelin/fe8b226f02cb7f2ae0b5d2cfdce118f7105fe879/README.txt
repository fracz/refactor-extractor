commit fe8b226f02cb7f2ae0b5d2cfdce118f7105fe879
Author: Jongyoul Lee <jongyoul@gmail.com>
Date:   Wed Feb 15 20:09:31 2017 +0900

    ZEPPELIN-2057 Extract InterpreterSetting functions from InterpreterFactory

    ### What is this PR for?
    Reducing size of InterpreterFactory and divide some functions from InterpreterFactory. Currently, InterpreterFactory has a lot of functions including management on InterpreterSetting and interpreter processes. This PR extracts InterpreterSetting-related parts into `InterpreterSettingManager`. It also has two unrelated functions: InterpreterSetting and InterpreterGroup. I'll treat it with another PR. This PR has some mechanical changes because it can help understand which parts has been changed. I'll do refactoring them later.

    ### What type of PR is it?
    [Refactoring]

    ### Todos
    * [x] - Extract all methods and variables related InterpreterSetting from InterpreterFactory

    ### What is the Jira issue?
    * https://issues.apache.org/jira/browse/ZEPPELIN-2057

    ### How should this be tested?
    Works exactly as same before

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Jongyoul Lee <jongyoul@gmail.com>

    Closes #2001 from jongyoul/ZEPPELIN-2057 and squashes the following commits:

    a3a844e [Jongyoul Lee] Set transient instance into InterpreterSetting
    1377ef6 [Jongyoul Lee] Set transient object into value objects
    0affba7 [Jongyoul Lee] Mapped the location of some functions related to InterpreterSetting again
    c378c3f [Jongyoul Lee] Fixed the style
    1e995f5 [Jongyoul Lee] Extracted InterpreterSetting from InterpreterFactory
    6b304a9 [Jongyoul Lee] ing...