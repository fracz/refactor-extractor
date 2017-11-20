commit 0e087455af491a1c4b0fd393ceed39489c790793
Author: Anthony Corbacho <corbacho.anthony@gmail.com>
Date:   Sat Nov 5 09:57:20 2016 +0900

    [ZEPPELIN-1615] - Zeppelin should be able to run without Shiro

    ### What is this PR for?
    Right now, Zeppelin use Shiro by default even if you dont need it.
    (It will use shiro.ini.template file if it doenst find shiro.ini), this behaviors is a little flacky and we should start zeppelin without shiro context if user doenst want to use it.

    ### What type of PR is it?
    [Bug Fix | Improvement ]

    ### Todos
    * [x] - Update configuration - Return empty if shiro file not found
    * [x] - refactor Rest Api handler, if shiro.ini not found start a handler without shiro context
    * [x] - refactor SecurityUtils to handle the case of shiro is disabled.

    ### What is the Jira issue?
     * [ZEPPELIN-1615](https://issues.apache.org/jira/browse/ZEPPELIN-1615)

    ### How should this be tested?
    Start zeppelin without shiro.ini

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Anthony Corbacho <corbacho.anthony@gmail.com>

    Closes #1595 from anthonycorbacho/fix/ShiroTempleteLoginRequired and squashes the following commits:

    c163bd8 [Anthony Corbacho] Handle the case of user already have a shiro ini :: backup current ini and perform tests then restaure
    608e718 [Anthony Corbacho] Fix AuthenticationIT Test by creating shiro.ini file in conf.
    73ce69a [Anthony Corbacho] Fix SecurityRestApiTest test
    4c67e8f [Anthony Corbacho] Handle SecurityUtils, if shiro is disabled then script all the getPrincipla and shiro check and return anon or empty collections in certain case
    f67f82e [Anthony Corbacho] Handle the case of user want to start zeppelin without Shiro  - Refactor handler, if not shiro ini foundm start zeppelin wihtout shiro
    09387ce [Anthony Corbacho] Rework getShiroIni in zeppelinConfiguration, if shiro.ini file is not found, then return empty instead of shiro.ini.template path