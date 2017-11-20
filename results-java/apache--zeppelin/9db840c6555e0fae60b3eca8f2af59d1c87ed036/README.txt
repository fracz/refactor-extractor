commit 9db840c6555e0fae60b3eca8f2af59d1c87ed036
Author: CloverHearts <cloverheartsdev@gmail.com>
Date:   Tue Nov 29 00:09:33 2016 +0900

    [ZEPPELIN-1665] Z.run with external note executable and access resource for zeppelin in each interpreter

    ### What is this PR for?
    Currently, the z.run command is restricted.
    Only paragraphs in a single note can be executed.
    I have modified this to allow you to freely execute paragraphs of other notes.
    This PR provides the basis for the freeful use of Zeppelin's resources at each Interpreter implementation.

    ### What type of PR is it?
    Improvement, Feature

    ### Todos
    - [x] extends z.run
    - [x] run all paragraph in external note
    - [x] run paragraph for external note.
    - [x] get resource for zeppelin in each interpreter.
    - [x] improve test case.
    - [x] how to use docuement

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-1665

    ### How should this be tested?
    Currently under development.

    run paragraph in same note
    ```
    %spark
    z.run("paragraphID")
    ```

    run paragraph with external note
    ```
    z.run("noteid", "paragraphid");
    ```

    all note run
    ```
    z.runNote("noteid");
    ```

    ### Screenshots (if appropriate)
    - paragraph run
    ![zrun](https://cloud.githubusercontent.com/assets/10525473/20304857/ca056300-ab75-11e6-8276-0fe0667a5a24.gif)

    - noterun
    ![runnote](https://cloud.githubusercontent.com/assets/10525473/20472104/527cd8de-affa-11e6-9587-0438140e264f.gif)

    ### Questions:
    * Does the licenses files need update? no
    * Is there breaking changes for older versions? no
    * Does this needs documentation? yes

    Author: CloverHearts <cloverheartsdev@gmail.com>

    Closes #1637 from cloverhearts/extends-zrun-remote-transaction and squashes the following commits:

    41fa9d7 [CloverHearts] restore unless changed and import
    113b475 [CloverHearts] Merge branch 'master' into extends-zrun-remote-transaction
    03a3a2b [CloverHearts] testcase change z.run(2, context) to z.run(2)
    2a2c173 [CloverHearts] Merge branch 'master' into extends-zrun-remote-transaction
    f2e3bcf [CloverHearts] fix TestCase
    5a80a5a [CloverHearts] last test case time check to print string
    e6cd82c [CloverHearts] Merge branch 'master' into extends-zrun-remote-transaction
    3862166 [CloverHearts] regenerate thrfit class
    5ec4640 [CloverHearts] change defined protocol for thrift
    7562535 [CloverHearts] remove unused import and asterisk import
    8a54917 [CloverHearts] Merge branch 'master' into extends-zrun-remote-transaction
    342752d [CloverHearts] add document for extends z.run and z.runNote
    292319a [CloverHearts] add test case for extends z.run and z.runNote
    10c2a47 [CloverHearts] Implement runNote and re implement run method
    f9661c8 [CloverHearts] Merge branch 'master' into extends-zrun-remote-transaction
    9ab05af [CloverHearts] Change structure and remove remoteWorksManager
    8cbe46c [CloverHearts] remote remoteworksController in interpreter.java
    8d42c16 [CloverHearts] Merge branch 'master' into extends-zrun-remote-transaction
    f11fed4 [CloverHearts] Merge branch 'workflow' into extends-zrun-remote-transaction
    c074f07 [CloverHearts] fix sio support
    4b1ef08 [CloverHearts] fix thrift interface
    2628a20 [CloverHearts] fix thrift
    6fbe08a [CloverHearts] Merge branch 'master' into workflow
    3f75bd5 [CloverHearts] support scald
    55e8704 [CloverHearts] support spark r
    5a7886f [CloverHearts] fix sio support
    afb9db7 [CloverHearts] Merge branch 'master' into workflow
    3ed556c [CloverHearts] remove debug console message.
    3d34f9e [CloverHearts] Implement getParagraphRunner transaction.
    2523238 [CloverHearts] Implement eventForWait class
    0570ae8 [CloverHearts] add remote works controller class and include interpreter factory
    6e1f219 [CloverHearts] code base workflow for remote zeppelin server control default thrift transaction.