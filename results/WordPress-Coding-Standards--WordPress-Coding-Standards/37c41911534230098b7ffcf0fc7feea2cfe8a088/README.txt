commit 37c41911534230098b7ffcf0fc7feea2cfe8a088
Author: jrfnl <jrfnl@users.noreply.github.com>
Date:   Tue Aug 8 15:30:14 2017 +0200

    AssignmentInCondition sniff: Detect assignments in ternary conditions

    In the original PR, the conditions for ternaries were explicitly not examined by this sniff.

    This PR changes that. As long as either the condition, i.e. the part before the `?` is within parentheses, òr the complete ternary is within parentheses, they will now be examined and throw the appropriate errors.

    I suspect this may still be a little buggy with parentheses not belonging to the ternary, but with a ternary somewhere within them allowing this sniff to trigger.

    More unit test cases very welcome!

    For now, I'm only pulling this in WPCS. Once the worst bugs have been removed, I will upstream this improvement to the PHPCS version as well.