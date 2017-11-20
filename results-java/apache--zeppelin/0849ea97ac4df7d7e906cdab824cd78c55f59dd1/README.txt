commit 0849ea97ac4df7d7e906cdab824cd78c55f59dd1
Author: Bruno Bonnin <bbonnin@gmail.com>
Date:   Mon Dec 28 15:47:42 2015 +0100

    Display aggregation result with Elasticsearch interpreter

    ### What is this PR for?
    Take into account the aggregations with Elastisearch search query.
    This improvement of the interpreter will return the result of the aggregation part if any in the result of search query.

    ### What type of PR is it?
    Improvement

    ### Todos
    * [X ] - Modify interpreter to read and return the aggregation result if any
    * [X ] - Update docs

    ### Is there a relevant Jira issue?
    No

    ### How should this be tested?
    Test with an Elasticsearch and with examples provided in the doc.

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? elasticsearch.md

    Author: Bruno Bonnin <bbonnin@gmail.com>

    Closes #596 from bbonnin/master and squashes the following commits:

    aa809e2 [Bruno Bonnin] Update elasticsearch.md
    6793232 [Bruno Bonnin] Update elasticsearch.md
    17f4e58 [Bruno Bonnin] Merge branch 'master' of https://github.com/bbonnin/incubator-zeppelin
    6f76ffd [Bruno Bonnin] Support of aggregation results