commit 251314e541929741a34cc816eec7033f93096245
Merge: 2655072 5a571b6
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Jul 31 18:40:00 2015 +0200

    feature #15160 Redesigned the web debug toolbar (javiereguiluz)

    This PR was merged into the 2.8 branch.

    Discussion
    ----------

    Redesigned the web debug toolbar

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    In my opinion, the design of the current Web Debug Toolbar suffers from two problems: it looks outdated and it lacks polishing on some parts. Symfony has always introduced a big update in this toolbar for each big release, so I propose to do the same for Symfony 3.

    Let's see the new proposed toolbar in action:

    -----

    **This is the default toolbar** (click on the image to enlarge it)

    ![default](https://cloud.githubusercontent.com/assets/73419/8449444/cb4be4ea-1fd1-11e5-9358-332449375531.png)

    As you can see, if some panel doesn't provide any information, we don't show it (e.g. 0 queries, 0 forms, 0 logs, 0 ajax requests).

    -----

    Let's compare some panels in detail:

    **Symfony Panel**

    ![symfony-old-panel](https://cloud.githubusercontent.com/assets/73419/8449504/407302b2-1fd2-11e5-80da-a49dc99e68f5.png) ![symfony-new-panel](https://cloud.githubusercontent.com/assets/73419/8449505/41698e20-1fd2-11e5-8ced-e2717716c2d3.png)

    **Request Panel**

    ![request-old-panel](https://cloud.githubusercontent.com/assets/73419/8449531/7d6888ea-1fd2-11e5-829e-93c9e50b7b64.png) ![request-new-panel](https://cloud.githubusercontent.com/assets/73419/8449532/7e359da8-1fd2-11e5-87e6-f194d824254c.png)

    -----

    When needed, more panels are displayed, such as the SQL information:  (click on the image to enlarge it)

    ![sql_panel](https://cloud.githubusercontent.com/assets/73419/8449554/b6abc1d0-1fd2-11e5-8450-13eae5bc3c50.png)

    As you can see, the new toolbar provides more information than the old one and it takes less space.

    Some of the new panels include more information in the extended info, such as the Doctrine one showing that the second level cache is disabled:

    ![sql_old_panel](https://cloud.githubusercontent.com/assets/73419/8449573/fc288aae-1fd2-11e5-9823-46b33fa9d998.png) ![sql_new_panel](https://cloud.githubusercontent.com/assets/73419/8449572/fab6244c-1fd2-11e5-9e14-28045de0b143.png)

    -----

    **Errors and warnings** now stand out more clearly because all the panel background is changed.

    For example, if there are i18n errors: (click on the image to enlarge it)

    ![i18n_panel_error](https://cloud.githubusercontent.com/assets/73419/8449590/20708786-1fd3-11e5-8ad7-6bd3b3f4b933.png)

    If the page is loading too slowly: (click on the image to enlarge it)

    ![slow_page](https://cloud.githubusercontent.com/assets/73419/8449597/2bc0c6e6-1fd3-11e5-9a9e-55bfe07ac170.png)

    If Symfony version is deprecated: (click on the image to enlarge it)

    ![deprecated_symfony](https://cloud.githubusercontent.com/assets/73419/8449605/3cfa9860-1fd3-11e5-8dbd-9915597970f4.png)

    -----

    HTTP Errors also stand out more clearly: (click on the image to enlarge it)

    ![error_404](https://cloud.githubusercontent.com/assets/73419/8449617/56a4ccb8-1fd3-11e5-9638-322f1840937c.png)

    ![error_500](https://cloud.githubusercontent.com/assets/73419/8449618/57a79294-1fd3-11e5-8383-2598dee73fc6.png)

    -----

    Some questions that you may be wondering:

      * **Why use a dark toolbar instead of maintaining the light toolbar?** Because a dark toolbar stands out more from most of the web designs. It's more probable that your applications display a light background than a dark background, so the Symfony Toolbar stands out more if it's designed in dark.
      * **What about the profiler?** If this proposal is approved I'll also update the design of the profiler to match this new dark and modern look-and-feel.
      * **What about smaller screens?** This is a proposal, so I haven't finished it. Tweaking the design for smaller screens will be the next step. Anyway, as you can see the new toolbar already takes much less space than the current one, so it won't be hard to adapt it.

    Commits
    -------

    5a571b6 Reordered the toolbar elements via service priorities
    f237ff1 Increased the z-index of .sf-toolbar-info
    b3ad83d Removed an unused media query
    b438ee5 Redesigned "abbr" elements
    7d92cb8 Restored the old behavior for toolbars with lots of elements
    597637e Tweaks and bug fixes
    9df0f8b Added some upgrade notes about the new toolbar design
    22f6bc5 Removed an useless CSS class and added styles for <hr>
    5070861 Added a new profiler_markup_version to improve BC of the new toolbar
    2fb3319 Removed an unused import
    7ec1cd4 Reverted the feature to display different toolbar versions
    084cca6 Minor JavaScript optimizations
    972a92e Misc. tweaks and improvements
    ebb44e4 Added some styles to make old panels look better in the new design
    1847285 Pass the toolbar version number from the controller, to ease transition and keep BC
    a0e03f6 Minor tweaks
    002dda5 Fixed toolbar issues when displaying it inside the profiler
    e94a6a0 Smaller font sizes for smartphones, fixed request status padding issue and make too long panels always be displayed at the leftmost part of browser window
    9b585b9 Made the close icon a bit smaller
    3ab2e20 fixed all vertical aligning issues and tweaked icons
    f087ac0 More vertical aligning fixes
    9e38a8a Minor CSS tweaks and made font sizes bigger
    0dfcb60 Fixed an issue with the Config panel in the Profiler view
    cd53210 Fixed another z-index issue
    e28f895 A very high z-index value is required to avoid issues in the profiler view
    23dc884 Fixed a potential issue in the request panel
    7c35d25 Fixed another insignificant syntax issue
    e14fb6d Fixed a minor syntax issue
    9d89841 Finished the toolbar redesign
    b25b6dd Finished "dump" panel and other minor tweaks
    2bccdd4 Minor CSS fixes
    c0bee9b Tweaked the Twig panel
    77d522a Tweaked the translation panel
    041d424 Improved the Security toolbar panel
    af3dcb2 Minor Ajax tweaks
    acee052 Finished the Ajax panel redesign
    fac5391 Lots of minor improvements
    ef53850 More fixes and tweaks
    51a79c9 Reorder toolbar panels
    2735346 Fixed a minor markup error that broke the toolbar
    64b8f38 A new batch of updates
    4eee931 Restored all the code removed by mistake
    b6f413f First batch of fixes
    c2fcadc Redesigned the web debug toolbar