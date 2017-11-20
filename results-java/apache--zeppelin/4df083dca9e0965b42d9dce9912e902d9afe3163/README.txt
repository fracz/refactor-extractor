commit 4df083dca9e0965b42d9dce9912e902d9afe3163
Author: Zhong Wang <wangzhong.neu@gmail.com>
Date:   Thu Feb 25 21:30:57 2016 -0800

    Add checkbox as a type of dynamic forms

    ### What is this PR for?
    1. Add checkbox support in dynamic forms
    2. Fix ZEPPELIN-699: Cannot select the first item of dynamic forms when it is just created

    ### What type of PR is it?
    Feature & Bug Fix

    ### Todos
    * [x] Compatibility test with notes from previous versions
    * [x] Documentation
    * [x] Nice CSS layout
    * [x] Support checkbox creation by string substitution
    * [x] Support pyspark

    ### Is there a relevant Jira issue?

    https://issues.apache.org/jira/browse/ZEPPELIN-671
    https://issues.apache.org/jira/browse/ZEPPELIN-669

    ### How should this be tested?
    1. Create a %spark paragraph: `val a = z.checkbox("my_check", Seq(("a", "a"), ("b", "b"), ("c", "c")))`
    2. Toggle the checkboxes and see the outcome

    ### Screenshots (if appropriate)
    ![igojnqh0mz](https://cloud.githubusercontent.com/assets/3282033/13136828/929ec1ec-d5d1-11e5-89d9-98659f444f37.gif)

    ### Questions:
    * Does the licenses files need update?

    NO

    * Is there breaking changes for older versions?

    Sort of. I am not sure whether Input.type (form.type) has already been used in other places

    * Does this needs documentation?

    YES

    Author: Zhong Wang <wangzhong.neu@gmail.com>
    Author: Zhong Wang <zhongwang@Zhongs-MacBook-Pro.local>

    Closes #713 from zhongneu/dynamic-forms-checkbox and squashes the following commits:

    0d3e566 [Zhong Wang] change css class name from checkbox-group to checkbox-item
    584bab8 [Zhong Wang] some cleanups & fix an issue of obsolete values
    790d0f0 [Zhong Wang] add pyspark support
    2af3e64 [Zhong Wang] fix docs
    c0683f1 [Zhong Wang] add documentation for checkbox forms
    9336b61 [Zhong Wang] refactoring the form parsing / substitution code to support delimiter for multi-selection forms
    8035cb1 [Zhong Wang] fix a display issue in query mode if no display name is specified
    2a634be [Zhong Wang] fix an issue with invalid options: related to ZEPPELIN-674
    db62ca7 [Zhong Wang] revoke changes for hide/show; improve compatibility of older versions of notebooks
    b799fb0 [Zhong Wang] add option to configure hidden behavior for checkboxes
    f10d6e2 [Zhong Wang] better CSS layout & add show/hide option
    3273230 [Zhong Wang] fix a bug: the checkbox should show display name
    e190707 [Zhong Wang] fix several bugs, including ZEPPELIN-669
    6969e8c [Zhong Wang] first attempt of adding checkbox to dynamic forms