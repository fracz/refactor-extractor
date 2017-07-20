commit ea376685ae9e539efd1b8063cc4703f9354549b0
Author: Joshua Spence <josh@joshuaspence.com>
Date:   Sun Apr 5 22:29:39 2015 +1000

    Fix some odd looking arrays

    Summary: These arrays looks a little odd, most likely due to the autofix applied by `ArcanistXHPASTLinter::LINT_ARRAY_SEPARATOR`. See D12296 in which I attempt to improve the autocorrection from this linter rule.

    Test Plan: N/A

    Reviewers: epriestley, #blessed_reviewers

    Reviewed By: epriestley, #blessed_reviewers

    Subscribers: epriestley

    Differential Revision: https://secure.phabricator.com/D12281