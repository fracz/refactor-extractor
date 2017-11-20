commit 4eccfcd5259a6e4f1df3a66927950c6ec30a37af
Author: Renjith Kamath <renjith.kamath@gmail.com>
Date:   Tue May 10 10:01:37 2016 +0530

    ZEPPELIN-830 Improve table display to handle large data

    ### What is this PR for?
    This is an improvement for table display. By using [Handsontable](https://github.com/handsontable/handsontable) we can load and display more data in paragraphs. Tested using sample tabular data consisting 10000x80 cells

    ### What type of PR is it?
    Improvement

    ### Todos
    * [x] - Decimal formatting
    * [x] - Rendering without angular/html directives
    * [x] - UI fixes
    * [x] - License doc update

    ### What is the Jira issue?
    * https://issues.apache.org/jira/browse/ZEPPELIN-830

    ### How should this be tested?
    TODO : test cases to follow

    ### Screenshots (if appropriate)
    ![apr 26 2016 17 38](https://cloud.githubusercontent.com/assets/2031306/14817566/c034ab28-0bd5-11e6-8b7a-2216ceca8153.gif)

    ### Questions:
    * Does the licenses files need update? Yes
    * Is there breaking changes for older versions? No
    * Does this needs documentation? Maybe

    Author: Renjith Kamath <renjith.kamath@gmail.com>

    Closes #858 from r-kamath/ZEPPELIN-830 and squashes the following commits:

    f13ba91 [Renjith Kamath] Merge branch 'master' of https://github.com/apache/incubator-zeppelin into ZEPPELIN-830
    dd0ced7 [Renjith Kamath] ZEPPELIN-830 fix wrong variable name
    4fbb22b [Renjith Kamath] Merge branch 'master' of https://github.com/apache/incubator-zeppelin into ZEPPELIN-830
    2e422ea [Renjith Kamath] ZEPPELIN-830 update licence doc
    adb8380 [Renjith Kamath] ZEPPELIN-830 remove unused floatThead dep
    a96c94a [Renjith Kamath] ZEPPELIN-830 fix rendering without angular/html directives
    b365e7f [Renjith Kamath] ZEPPELIN-830 fix test
    b87245a [Renjith Kamath] ZEPPELIN-830 fix jshint error
    de6d134 [Renjith Kamath] ZEPPELIN-830 License update
    7bb508e [Renjith Kamath] ZEPPELIN-830 fix number formatting for table display
    4e6beb8 [Renjith Kamath] fix selenium test failure
    c833f6e [Renjith Kamath] ZEPPELIN-830 Improve table display to handle large data