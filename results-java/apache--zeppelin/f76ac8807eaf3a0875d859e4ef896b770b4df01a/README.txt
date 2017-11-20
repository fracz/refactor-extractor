commit f76ac8807eaf3a0875d859e4ef896b770b4df01a
Author: Guillermo Cabrera <guicaro@gmail.com>
Date:   Mon Mar 20 16:00:52 2017 +0300

    [ZEPPELIN-1720] Adding tests to verify behaviour of dynamic forms

    ### What is this PR for?
    Adding Selenium tests to ensure proper behaviour of dynamic forms.

    ### What type of PR is it?
    Test

    ### Todos
    N/A

    ### What is the Jira issue?
    [ZEPPELIN-1720](https://issues.apache.org/jira/browse/ZEPPELIN-1720)

    ### How should this be tested?

    1. Once should first get Firefox v. 41 as it is the latest version that works with the current version of Selenium in Apache Zeppelin.
    2. You can then run the tests with following command:

    `TEST_SELENIUM="true" mvn package -DfailIfNoTests=false -pl 'zeppelin-interpreter,zeppelin-zengine,zeppelin-server' -Dtest=ParagraphActionsIT
    `
    ### Screenshots (if appropriate)
    N/A

    ### Questions:
    * Does the licenses files need update? no
    * Is there breaking changes for older versions? no
    * Does this needs documentation? no

    Author: Guillermo Cabrera <guicaro@gmail.com>

    Closes #2141 from guicaro/ZEPPELIN-1720-AddingTestsForDynamicForms and squashes the following commits:

    a5bc7db [Guillermo Cabrera] Updated tests to work with new paragraph behaviour
    3fa2033 [Guillermo Cabrera] Updated testMultipleDynamicFormsSameType based on new behaviour of paragraphs
    92102bd [Guillermo Cabrera] Updated testSingleDynamicFormSelectForm for "Run on selection change" option
    f194979 [Guillermo Cabrera] Fixing other issues on test case messages for readability
    f7e99ca [Guillermo Cabrera] Corrected grammar in a test case output message
    bc1e7f9 [Guillermo Cabrera] Removed unused import, alignment and removed unnecesary condition in test case
    274b2c1 [Guillermo Cabrera] Added tests that cover behaviour of dynamic forms
    eed42f0 [Guillermo Cabrera] Completed and verified corrct behaviour of testSingleDynamicFormTextInput
    7a4f121 [Guillermo Cabrera] Added method stubbs for new UI tests checking correct behaviour of dynamic forms