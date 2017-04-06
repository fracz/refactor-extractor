commit eda821a87a7d58aea9eb41c70ca5a1569f5a8678
Merge: 2dfd136 3592d0d
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Mar 24 15:59:01 2017 -0700

    feature #22129 [WebProfilerBundle] Improve cache panel (ro0NL)

    This PR was squashed before being merged into the 3.3-dev branch (closes #22129).

    Discussion
    ----------

    [WebProfilerBundle] Improve cache panel

    | Q             | A
    | ------------- | ---
    | Branch?       | master
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #... <!-- #-prefixed issue number(s), if any -->
    | License       | MIT
    | Doc PR        | symfony/symfony-docs#... <!--highly recommended for new features-->

    An attempt to improve the page a bit. Personally i think all elements on a single page is too much info.

    Before

    ![image](https://cloud.githubusercontent.com/assets/1047696/24272477/d4d96a44-101d-11e7-9cc5-1646fc2c0603.png)

    ![image](https://cloud.githubusercontent.com/assets/1047696/24272500/e51318a6-101d-11e7-8875-c270016f11a2.png)

    After

    ![image](https://cloud.githubusercontent.com/assets/1047696/24311179/530dcc6a-10d3-11e7-9c39-7c73ee2775f1.png)

    ![image](https://cloud.githubusercontent.com/assets/1047696/24311215/82c48566-10d3-11e7-82ff-6d79c3040a25.png)

    Commits
    -------

    3592d0de6a [WebProfilerBundle] Improve cache panel