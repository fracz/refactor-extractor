commit 6a1667dc5d714e886f5e2d927773098e1d3299ba
Merge: b819518 31b609e
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Oct 19 15:03:14 2016 -0700

    feature #19982 [Validator] Allow validating multiple groups in one GroupSequence step (enumag)

    This PR was merged into the 3.2-dev branch.

    Discussion
    ----------

    [Validator] Allow validating multiple groups in one GroupSequence step

    | Q             | A
    | ------------- | ---
    | Branch?       | master
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets |
    | License       | MIT
    | Doc PR        |

    To see what I've changed just look at the last commit. The other two are just refactoring without any effect.

    GroupSequenceProviderInterface seems to be the recommended solution for conditional validators (like when some properties should be required only if other property has a certain value). And it's a good solution honestly. Except for the fact that it's completely useless when I want to get validation violations for all groups at once and not just violations for the first group that caused any violations.

    ```php
    // If the User validation group causes any violations the Premium group will not be
    // validated at all even if the $this->isPremium() is true.
    class User implements GroupSequenceProviderInterface {
        public function getGroupSequence() {
            $groups = array('User');
            if ($this->isPremium()) {
                $groups[] = 'Premium';
            }
            return $groups;
        }
    }
    ```

    To be honest I never found a use case for this step-by-step behavior. When user fills a form I want to show him all errors at once not just some subset.

    It's surprisingly easy to fix this. With just one changed line in RecursiveContextualValidator it's perfectly solveable:

    ```php
    // With this PR it is possible to do this and get violations for both groups like this.
    class User implements GroupSequenceProviderInterface {
        public function getGroupSequence() {
            $groups = array('User');
            if ($this->isPremium()) {
                $groups[] = 'Premium';
            }
            return [$groups]; // this line has changed
        }
    }
    ```

    Commits
    -------

    31b609e [Validator] Allow validating multiple groups in one GroupSequence step
    3847bad [Validator] Refactor tests
    38b643a [Validator] Fix annotation