commit 807d19207cc2c14c6f74aef9f029e718ea4dbc1d
Merge: 53d9003 a276eb0
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Jun 8 20:19:55 2015 +0200

    minor #14905 [DX][Form] Show the class name when the deprecated setDefaultOptions is used (peterrehm)

    This PR was merged into the 2.7 branch.

    Discussion
    ----------

    [DX][Form] Show the class name when the deprecated setDefaultOptions is used

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    After upgrading to 2.7 I got plenty of deprecation messages which I could not assign directly as I have updated all of my FormTypes.

    ![bildschirmfoto 2015-06-07 um 12 02 22](https://cloud.githubusercontent.com/assets/2010989/8024784/8108aeee-0d0d-11e5-8f24-415c553ccb4c.png)

    Whit this minor improvement the actual class will be show so the upgrade will be much easier.

    ![bildschirmfoto 2015-06-07 um 12 01 24](https://cloud.githubusercontent.com/assets/2010989/8024788/93ff37d4-0d0d-11e5-8689-c3e8a933102b.png)

    I think same should be considered in other deprecation errors as it gets more difficult to trace down if external libraries are involved.

    Commits
    -------

    a276eb0 Show the FormType and FormTypeExtension in case of deprecated use of setDefaultOptions