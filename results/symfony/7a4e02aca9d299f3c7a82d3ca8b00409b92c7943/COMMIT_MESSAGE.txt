commit 7a4e02aca9d299f3c7a82d3ca8b00409b92c7943
Merge: 2a8268c 42b95df
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Aug 27 15:03:54 2014 +0200

    feature #11346 [Console] ProgressBar developer experience (stefanosala, gido)

    This PR was merged into the 2.6-dev branch.

    Discussion
    ----------

    [Console] ProgressBar developer experience

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | yes
    | Tests pass?   | yes
    | Fixed tickets | #11184
    | License       | MIT
    | Doc PR        | WIP

    ## TODO

    - [x] Create `getProgress/setProgress` methods to replace `getStep/setCurrent`
    - [x] `ProgressBar::setCurrent` should auto-start the ProgressBar.
    - [x] You should be able to pass `max` to `start`
    - [x] `barCharOriginal` not needed. Logic can simply be part of `getBarChar`
    - [x] `getStepWidth` is internal information that should not be public
    - [x] when verbosity set to quiet, the progress bar does not even need to execute all the logic to generate output that is then thrown away
    - [x] Allow to advance past max.
    - [x] negative max needs to be validated
    - [x] `getProgressPercent` should return float instead of int.

    Commits
    -------

    42b95df [Console][ProgressBar] Developer experience:  - Removed barCharOriginal  - getProgressPercent should return float instead of int.  - Minor refactoring
    3011685 [Console][ProgressBar] Allow to advance past max.
    73ca340 [Console][ProgressBar] Developer experience  - Create getProgress/setProgress methods to replace getStep/setCurrent  - ProgressBar::setCurrent should auto-start the ProgressBar.  - You should be able to pass max to start  - getStepWidth is internal information that should not be public  - when verbosity set to quiet, the progress bar does not even need to    execute all the logic to generate output that is then thrown away