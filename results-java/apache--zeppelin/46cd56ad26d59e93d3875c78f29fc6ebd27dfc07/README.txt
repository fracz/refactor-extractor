commit 46cd56ad26d59e93d3875c78f29fc6ebd27dfc07
Author: 1ambda <1amb4a@gmail.com>
Date:   Wed Sep 21 10:20:58 2016 +0900

    [ZEPPELIN-1387] Support table syntax in markdown interpreter

    ### What is this PR for?

    Support table markdown syntax issued by [ZEPPELIN-1387](https://issues.apache.org/jira/browse/ZEPPELIN-1387?jql=project%20%3D%20ZEPPELIN)

    ### What type of PR is it?
    [Bug Fix | Improvement]

    This PR can be categorized as bug fix and improvement since it not only resolves the above issue but also support other markdown syntaxes.

    ### Todos
    * [ ] - Check the license issue of the [pegdown](https://github.com/sirthias/pegdown) library introduced by this PR

    ### What is the Jira issue?

    [ZEPPELIN-1387](https://issues.apache.org/jira/browse/ZEPPELIN-1387?jql=project%20%3D%20ZEPPELIN)

    ### How should this be tested?

    Write markdown texts and compare them with expected html DOMs. I'v also included some tests for this PR.

    ### Screenshots (if appropriate)

    <img width="708" alt="markdown" src="https://cloud.githubusercontent.com/assets/4968473/18061274/1f2be526-6e5d-11e6-9f1a-3528f3958d2c.png">

    ### Questions:

    * Does the licenses files need update?
    * Does coding style is appropriate?

    ### Additional Comments

    We might solve this issue by implementing custom table plugin for markdown4j by referring [the existing work of txtmark](https://github.com/zhenchuan/txtmark/commit/178486805e78e3d572b071ca8b9f8887a066edef).
    But I think it is not good idea in regard to coverage, maintainability and efficiency since markdown4j is currently not developed actively and it costs to implement all markdown plugins which is not supported by markdown4j.

    Author: 1ambda <1amb4a@gmail.com>

    Closes #1384 from 1ambda/fix-zeppelin-1387 and squashes the following commits:

    16cda72 [1ambda] fix: Merge with 3c8158 to resolve CI failure
    e6d41c8 [1ambda] fix: Resolve merge conflict with 8f344db
    e08929a [1ambda] fix: Handle more specific exception in catch block
    8b1e017 [1ambda] chore: Move github-markdown-css license to bin_licenses
    4d1cb3c [1ambda] fix: Typo in docs/interpreter/markdown.md
    85a5e3a [1ambda] fix: Use bower to install github-markdown-css
    297733f [1ambda] fix: Modify github-markdown-css license
    947a92a [1ambda] chore: Add license to newly created java files
    d228423 [1ambda] docs: Update markdown docs config, examples
    2b6516c [1ambda] feat: Support markdown.parser.type attr in md
    d2d4455 [1ambda] style: Reformat using intellij-java-google-style
    bf9100d [1ambda] chore: Restore markdown4j dependency
    55a2f10 [1ambda] fix: Add MarkdownParser interface to support mulitple parsers
    c33c715 [1ambda] fix: Remove the ANCHORLINKS option
    9cf31d0 [1ambda] fix: Use markdown-body class (default)
    f741949 [1ambda] fix: Add styles for markdown
    603d3db [1ambda] fix: Add missing transitive deps for pegdown
    7aecdcb [1ambda] chore: Add pegdown to the binary license list
    fa14b3e [1ambda] style: Apply google java code style guide
    029f550 [1ambda] [ZEPPELIN-1387] Support table in markdown interpreter