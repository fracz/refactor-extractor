commit 0589e27e7bb84ec81e1438bcbf3f2fd80ee5a963
Author: 1ambda <1amb4a@gmail.com>
Date:   Mon Jan 30 12:44:55 2017 +0900

    [ZEPPELIN-2008] Introduce Spell

    ### What is this PR for?

    Implemented **Spell** as one of Helium categories. *Technically, it's the frontend interpreter* runs on browser not backend.

    Spell can provide many benefits.

    1. Anyone can install, remove easily using helium package registry by #1936
    2. Implementing spell is extremely easier rather than adding backend interpreter
    3. Can use existing javsacript libraries. (e.g [flowchart.js](http://flowchart.js.org/), [sequence diagram js](https://github.com/bramp/js-sequence-diagrams), ...). This enable us to add many visualization tools. Imagine that you can implement some custom interpreters with few lines of code like [flowchart-spell-example](https://github.com/apache/zeppelin/compare/master...1ambda:ZEPPELIN-2008/introduce-spell?expand=1#diff-364845b20d68e4d94688e44fef03da98)
    4. The most important thing is, spell is not only interpreter but also display system. Because it runs on browser. So we can use spell display system with another spell **Display System with Spell** (see the screenshot section below)

     **In future**, we will be able to combine existing backend interpreters with spell like (**not supported in this PR cause we need to modify backend code a lot**)

    ```
    // if we have markdown spell, we can use `%markdown` display in the spark interpreter

    %spark

    val calculated = doSomething()
    println(s"%markdown _${calculated})
    ```

    I added some examples. Checkout `echo`, `markdown`, `translator`, `flowchart` spells.

    ### What type of PR is it?
    [Feature]

    ### Todos
    * [x] - Add `SPELL` as one of Helium categories.
    * [x] - Implement framework code (`zeppelin-spell`)
    * [x] - Make some examples (flowchart, google translator, markdown, echo)
    * [x] - Support custom display system
    * [x] - Fix some bugs in `HeliumBundleFactory`
    * [x] - Save spell rendering result into `note.json` while broadcasting to other websocket clients
    * [x] - Fix `renderText` for stream output

    ### What is the Jira issue?

    [ZEPPELIN-2008](https://issues.apache.org/jira/browse/ZEPPELIN-2008)

    ### How should this be tested?

    - Build `mvn clean package -Phelium-dev -Pexamples -DskipTests;`
    - Go to helium page `http://localhost:8080/#/helium`
    - Enable all spells
    - Go to a notebook and refresh
    - Follow actions in the screenshots below.

    ### Screenshots (if appropriate)

    #### Flowchart Spell (Sample)

    ![flowchart-spell](https://cloud.githubusercontent.com/assets/4968473/22275041/305f0eb8-e2ed-11e6-846a-9f1263ae46bc.gif)

    #### Google Translator Spell (Sample)

    ![translator-spell](https://cloud.githubusercontent.com/assets/4968473/22280993/9820c238-e317-11e6-90f4-0e483312a09a.gif)

    #### Display System with Spell

    ![display-spell](https://cloud.githubusercontent.com/assets/4968473/22275044/33694b78-e2ed-11e6-9ef0-188f4038381f.gif)

    ### Questions:
    * Does the licenses files need update - NO
    * Is there breaking changes for older versions? - NO
    * Does this needs documentation? - YES, but framework can be enhanced so i would like to defer to write document right now.

    Author: 1ambda <1amb4a@gmail.com>

    Closes #1940 from 1ambda/ZEPPELIN-2008/introduce-spell and squashes the following commits:

    c1b5356 [1ambda] fix: RAT issues
    e07ecd3 [1ambda] fix: Set width for spell usage
    6c91892 [1ambda] feat: Display magic, usage for spell
    5be2890 [1ambda] feat: Support spell info
    822a1d8 [1ambda] style: Remove useless func wrap for helium
    35d0fcc [1ambda] fix: Update desc for spell examples
    49e03fc [1ambda] fix: List visualziation bundles only in order
    4029c02 [1ambda] fix: ParagraphIT, parameterizedQueryForm
    08eba10 [1ambda] refactor: renderGraph in result.controller.js
    69ce880 [1ambda] fix: Resolve append (stream) output
    0f2d8b6 [1ambda] fix: Resolve output issue
    fc4389e [1ambda] fix: Resolve RAT issues
    c8c8f0e [1ambda] fix: Add setErrorMessage method to Job
    4fec44c [1ambda] refactor: NotebookServer.java
    1227d7d [1ambda] refactor: result controller retry
    9fb7438 [1ambda] feat: Save spell result and propagate
    3cdf2da [1ambda] fix: NPM installation error
    72aadbf [1ambda] feat: Enhance translator spell
    bd2b3ef [1ambda] style: Rename generator -> data
    cac0667 [1ambda] style: Rename to Spell
    e81cb03 [1ambda] example: Add echo, markdown
    0fa7eda [1ambda] feat: Support custom display
    c906da6 [1ambda] feat: Update examples to use single FrontIntpRes
    5c49e6e [1ambda] feat: Automated display type checking in result
    5810bf1 [1ambda] feat: Apply frontend interpreter to paragraph
    a163044 [1ambda] feat: Add flowchart, translator examples
    247d00f [1ambda] feat: Add frontend interpreter framework
    e925967 [1ambda] feat: Support FRONTEND_INTERPRETER type in frontend
    c02d00a [1ambda] feat: Support FRONTEND_INTERPRETER type in backend