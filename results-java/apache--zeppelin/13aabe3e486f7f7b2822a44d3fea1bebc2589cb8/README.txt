commit 13aabe3e486f7f7b2822a44d3fea1bebc2589cb8
Author: Alexander Shoshin <Alexander_Shoshin@epam.com>
Date:   Fri Apr 7 12:02:07 2017 +0300

    [MINOR] Add paragraph to note

    ### What is this PR for?
    We need to add an ability of adding custom _Paragraph_ to a _Note_. This will make it easier to write _Note_ and _Paragraph_ tests. At the moment all paragraphs are created inside _Note_ class.

    This refactoring will allow to write a test for [ZEPPELIN-1856](https://issues.apache.org/jira/browse/ZEPPELIN-1856) issue.

    ### What type of PR is it?
    [Refactoring]

    ### Todos
    * [x] - Rename `addParagraph()` -> `addNewParagraph()` and `insertParagraph()` -> `insertNewParagraph()`. These names will describe what mathods do: create new paragraph and add it to the note.
    * [x] - Remove duplicated code from `addNewParagraph()` and `insertNewParagraph()` methods.
    * [x] - Add methods `addParagraph()` and `insertParagraph()`, which receive a _Paragraph_ instance in parameters and add it to the _Note_.

    ### Questions:
    * Does the licenses files need update? **no**
    * Is there breaking changes for older versions? **no**
    * Does this needs documentation? **no**

    Author: Alexander Shoshin <Alexander_Shoshin@epam.com>

    Closes #2233 from AlexanderShoshin/add-paragraph-to-note and squashes the following commits:

    676db15 [Alexander Shoshin] added methods addParagraph() and insertParagrepg()
    b0b1dd4 [Alexander Shoshin] remove duplicated code
    4391f6b [Alexander Shoshin] rename addParagraph() -> addNewParagraph(), insertParagraph() -> insertNewParagraph()