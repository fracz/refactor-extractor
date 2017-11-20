commit c66f90917955482e15db834566c242208dd361ba
Author: CloverHearts <cloverheartsdev@gmail.com>
Date:   Mon Jul 31 19:29:36 2017 +0900

    [ZEPPELIN-2818] Improve to better rendering from jupyter note

    ### What is this PR for?
    Hi, zeppelin community.
    Zeppelin currently has a way to use the Jupiter Note.
    This is a great feature.
    However, I have found that there are some problems with the way of showing.
    I've improved this to increase the user experience.
    Please check the screenshot for details.

    ### What type of PR is it?
    Improvement

    ### Todos
    - [x] added pre-render feature (markdown).
    - [x] increased to many support type for output data.

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-2818

    ### How should this be tested?
    1. build jupyter module
      `mvn clean package -DskipTests -pl 'zeppelin-jupyter' --am`
    2. `cd zeppelin-jupyter/target`
    3. `java -classpath zeppelin-jupyter-0.8.0-SNAPSHOT.jar org.apache.zeppelin.jupyter.JupyterUtil -i {your ipynb note file path!/getting_started.ipynb`
        (good sample : [go to sample](https://github.com/SciRuby/sciruby-notebooks/blob/master/getting_started.ipynb)
    4. get a `note.json` and import to zeppelin on frontend!
    5. enjoy

    ### Screenshots (if appropriate)
    #### Before
    ![oldold](https://user-images.githubusercontent.com/10525473/28717881-8c176d40-73de-11e7-9a67-732808957391.gif)

    #### After
    ![zeppelin-from-jupyternote](https://user-images.githubusercontent.com/10525473/28717782-1fa90f4c-73de-11e7-8d9c-f5191d8f8a47.gif)

    ### Questions:
    * Does the licenses files need update? no
    * Is there breaking changes for older versions? no
    * Does this needs documentation? no

    Author: CloverHearts <cloverheartsdev@gmail.com>

    Closes #2509 from cloverhearts/ZEPPELIN-2818 and squashes the following commits:

    4f915739f [CloverHearts] added apache docuement
    c15bbcdb7 [CloverHearts] improving test case
    c8b5a86cc [CloverHearts] fix command line interpreter tag
    f4cb0037f [CloverHearts] support single line text
    e9076133a [CloverHearts] add setter
    f077e69ae [CloverHearts] added supported single line output
    c77d9c913 [CloverHearts] add supported single string for source
    75c8ef540 [CloverHearts] import pegdown in pom
    b27bfc93f [CloverHearts] move interpreter to pegdown
    1e15581d1 [CloverHearts] improving reder feature
    a56b6ffc6 [CloverHearts] fix checkstyle
    4a5158603 [CloverHearts] implement markdown pre render
    6ce9935b9 [CloverHearts] improving get output part