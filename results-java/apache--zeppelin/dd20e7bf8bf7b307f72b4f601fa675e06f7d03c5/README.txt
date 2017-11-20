commit dd20e7bf8bf7b307f72b4f601fa675e06f7d03c5
Author: Mina Lee <minalee@apache.org>
Date:   Sat Nov 5 12:49:36 2016 +0900

    [ZEPPELIN-1564] Enable note deletion and paragraph output clear from main page

    ### What is this PR for?
    - Enables removing note and clear all paragraph's output from Zeppelin main page.
    - Add rest api for clearing all paragraph output

    Next possible improvement can be removing notes in folder level and rename folder.

    ### What type of PR is it?
    Improvement

    ### Todos
    * [x] - Merge #1567 and apply security to `clearAllParagraphOutput` rest api method

    ### What is the Jira issue?

    [ZEPPELIN-1564](https://issues.apache.org/jira/browse/ZEPPELIN-1564)
    ### Screenshots (if appropriate)

    ![oct-27-2016 18-26-03](https://cloud.githubusercontent.com/assets/8503346/19761938/e013ea02-9c72-11e6-9a08-0a70aca145d2.gif)
    ### Questions:
    - Does the licenses files need update? no
    - Is there breaking changes for older versions? no
    - Does this needs documentation? yes

    Author: Mina Lee <minalee@apache.org>

    Closes #1565 from minahlee/ZEPPELIN-1564 and squashes the following commits:

    749aebe [Mina Lee] Merge branch 'master' of https://github.com/apache/zeppelin into ZEPPELIN-1564
    1393ee9 [Mina Lee] Rename class name from UnauthorizedException to ForbiddenException Update clear output rest api doc response code
    2ee452e [Mina Lee] Add auth check before clearing all paragraph
    fb7e6ae [Mina Lee] Merge branch 'master' of https://github.com/apache/zeppelin into ZEPPELIN-1564
    f349dbf [Mina Lee] Change post to put
    7eb3521 [Mina Lee] Give writer permission to clear output
    dea3ef6 [Mina Lee] Remove unused import
    d66600c [Mina Lee] Add rest api endpoint for clear paragraph result to document
    3d19141 [Mina Lee] Add rest api for clear all paragraph result and add test
    98d7604 [Mina Lee] Add clearAllParagraphOutput unit test
    4adddb4 [Mina Lee] Clear all paragraphs and remove note from main page