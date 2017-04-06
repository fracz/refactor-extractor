commit 05273449cb866d774efaea4d3b5f01908994a2f4
Merge: 352049c 9859125
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Dec 22 16:06:00 2015 +0100

    bug #17109 Improved the design of the web debug toolbar (javiereguiluz)

    This PR was squashed before being merged into the 2.8 branch (closes #17109).

    Discussion
    ----------

    Improved the design of the web debug toolbar

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    This PR contains three little improvements:

    ## 1) Icon size

    Somehow, the size of the icons wasn't limited properly. This makes them appear too big and unaligns text vertically:

    ### Before

    ![before_icon_size](https://cloud.githubusercontent.com/assets/73419/11956693/5dce6432-a8bc-11e5-9726-958bbf95e945.png)

    ### After

    ![after_icon_size](https://cloud.githubusercontent.com/assets/73419/11956696/5faba396-a8bc-11e5-8453-6d9601803335.png)

    ## 2) Status blocks

    It's very common to have single-digit and double-digit status blocks in some panels. Now they show a different width:

    ![before_status_1](https://cloud.githubusercontent.com/assets/73419/11956743/affd180c-a8bc-11e5-8c53-044b60dad53e.png) ![before_status_2](https://cloud.githubusercontent.com/assets/73419/11956744/b0ed99c6-a8bc-11e5-83d6-7a926e780d14.png)

    Now we set a minimum-width that looks good for single and double digit values:

    ![after_status_1](https://cloud.githubusercontent.com/assets/73419/11956766/dff29758-a8bc-11e5-9eea-f6204acb2794.png) ![after_status_2](https://cloud.githubusercontent.com/assets/73419/11956768/e1af5f68-a8bc-11e5-8d07-e6219a667529.png)

    Although this solution doesn't solve the case when some block displays three digits, I think we can safely ignore that edge case.

    ## 3) Colors

    The green/yellow/red colors of the toolbar were different from the green/yellow/red colors of the profiler. The reason is that we designed the toolbar first and separately from the profiler. The second (minor) issue is that green and yellow didn't have enough contrast (they didn't pass the WCAG accessibility check).

    So I propose to slightly change the green and yellow colors and to use the same colors in the toolbar and the profiler.

    ### Toolbar Before

    ![before_colors](https://cloud.githubusercontent.com/assets/73419/11956842/83e1c618-a8bd-11e5-9ac0-b97de8e70ec3.png)

    ### Toolbar After

    ![after_colors](https://cloud.githubusercontent.com/assets/73419/11956844/86759af8-a8bd-11e5-83c5-7ce2f74d4b88.png)

    ### Profiler Before

    ![before_profiler_success](https://cloud.githubusercontent.com/assets/73419/11956864/c21889a8-a8bd-11e5-95ee-8d6103b6a26c.png)

    ![before_profiler_warning](https://cloud.githubusercontent.com/assets/73419/11956866/c3240e62-a8bd-11e5-8797-de3481dd40a0.png)

    ![before_profiler_error](https://cloud.githubusercontent.com/assets/73419/11956867/c4d7b25e-a8bd-11e5-9e7a-6519b34009de.png)

    ### Profiler After

    ![after_profiler_success](https://cloud.githubusercontent.com/assets/73419/11956887/ea5ceabc-a8bd-11e5-8373-492e838148ea.png)

    ![after_profiler_warning](https://cloud.githubusercontent.com/assets/73419/11956888/eb3bf284-a8bd-11e5-8be6-93a91ed2ae0e.png)

    ![after_profiler_error](https://cloud.githubusercontent.com/assets/73419/11956889/ed057b4e-a8bd-11e5-9533-a807d8547843.png)

    The new colors pass the accessibility requirements:

    ![color_accessibility_success](https://cloud.githubusercontent.com/assets/73419/11956894/03ccbd60-a8be-11e5-96d2-727bb9b62dc7.png)

    ![color_accessibility_warning](https://cloud.githubusercontent.com/assets/73419/11956896/04d893f0-a8be-11e5-8c18-9515f624974a.png)

    ![color_accessibility_error](https://cloud.githubusercontent.com/assets/73419/11956899/06cbc524-a8be-11e5-8622-e2b60da1c8b2.png)

    Commits
    -------

    9859125 Improved the design of the web debug toolbar