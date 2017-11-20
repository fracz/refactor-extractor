commit 7b585c7399f29492c308f83e342cac29b0c7ca07
Author: Jeff Zhang <zjffdu@apache.org>
Date:   Wed Apr 12 10:50:16 2017 +0800

    ZEPPELIN-2395. Refactor Input.java to make dynamic forms extensible

    ### What is this PR for?

    Currently, zeppelin only support 3 kinds of dynamic form controls: TextBox, Select, CheckBox. All the things are in `Input.java`, this is hard to add new controls, this PR is for refactoring Input to make dynamic forms extensible.  Main Changes:
    * Make `Input` as the base class of dynamic forms also use it as the factory class
    * All the concret dynamic forms extend `Input`
    * Add method `toJson` and `fromJson` for `GUI` for `GUI`'s serialization/deserialization. I plan to do it for other classes as well, so that we can remove duplicated serde code and also make it easy to test serialization/deserialization
    * Change `z.input` to `z.textbox` as I think z.input is a little misleading. But I still keep `z.input` and make `z.input` as deprecated.
    * Ideally the new input forms' json should be the same as the old input form json. But there's one bug in the old input form, `type` is missing if the input forms are created in frontend for textbox and select. So I keep the old input forms for compatibility. I will load the old input forms json and convert it into new input forms, and after saving, `note.json` would have the new input forms json.

    After this PR, user needs to do 3 things to add new ui controls
    * Implement its UI control classes, (refer TextBox/CheckBox/Select), and specify it in `TypeAdapterFactory` of `Input` for serde.
    * Add parsing logic in `Input.getInputForm` if you want to support this control in frontend.
    * Add display logic in `paragraph-parameterizedQueryForm.html`

    ### What type of PR is it?
    [ Improvement | Refactoring]

    ### Todos
    * [ ] - Task

    ### What is the Jira issue?
    * https://issues.apache.org/jira/browse/ZEPPELIN-2395

    ### How should this be tested?
    Test is added

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? Yes
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Jeff Zhang <zjffdu@apache.org>

    Closes #2245 from zjffdu/ZEPPELIN-2395 and squashes the following commits:

    16d42a8 [Jeff Zhang] ZEPPELIN-2395. Refactor Input.java to make dynamic forms extensible