commit b10c38a37b46872800bb9576d464fb10759179db
Author: tjhunt <tjhunt>
Date:   Mon Jun 30 16:56:49 2008 +0000

    MDL-15452 - ongoing - Put the OU quiz navigation improvements into the Moodle codebase

    * Javadoc comments for most of the code I committed on Friday.
    * Implement the get_question_status method.
    * Along the way, refactor duplicated logic between two of the question types, questionlib, and the new code - removing inconsistency while doing so.