<?php declare(strict_types=1);
namespace Phan;

use Phan\Language\Context;
use Phan\Language\Element\Clazz;
use Phan\Language\Element\Method;
use Phan\Language\Element\Func;
use ast\Node;

/**
 * Plugins can be defined in the config and will have
 * their hooks called at appropriate times during analysis
 * of each file, class, method and function.
 *
 * Plugins must extends this class and return an instance
 * of themselves.
 */
abstract class PluginV2 {

    /**
     * Emit an issue if it is not suppressed
     *
     * @param CodeBase $code_base
     * The code base in which the issue was found
     *
     * @param Context $context
     * The context in which the issue was found
     *
     * @param string $issue_type
     * A name for the type of issue such as 'PhanPluginMyIssue'
     *
     * @param string $issue_message_fmt
     * The complete issue message format string to emit such as
     * 'class with fqsen {CLASS} is broken in some fashion' (preferred)
     * or 'class with fqsen %s is broken in some fashion'
     * The list of placeholders for between braces can be found
     * in \Phan\Issue::uncolored_format_string_for_template.
     *
     * @param string[] $issue_message_args
     * The arguments for this issue format.
     * If this array is empty, $issue_message_args is kept in place
     *
     * @param int $severity
     * A value from the set {Issue::SEVERITY_LOW,
     * Issue::SEVERITY_NORMAL, Issue::SEVERITY_HIGH}.
     *
     * @param int $remediation_difficulty
     * A guess at how hard the issue will be to fix from the
     * set {Issue:REMEDIATION_A, Issue:REMEDIATION_B, ...
     * Issue::REMEDIATION_F} with F being the hardest.
     */
    public function emitIssue(
        CodeBase $code_base,
        Context $context,
        string $issue_type,
        string $issue_message_fmt,
        array $issue_message_args = [],
        int $severity = Issue::SEVERITY_NORMAL,
        int $remediation_difficulty = Issue::REMEDIATION_B,
        int $issue_type_id = Issue::TYPE_ID_UNKNOWN
    ) {
        $issue = new Issue(
            $issue_type,
            Issue::CATEGORY_PLUGIN,
            $severity,
            $issue_message_fmt,
            $remediation_difficulty,
            $issue_type_id
        );

        $issue_instance = new IssueInstance(
            $issue,
            $context->getFile(),
            $context->getLineNumberStart(),
            $issue_message_args
        );

        Issue::maybeEmitInstance(
            $code_base,
            $context,
            $issue_instance
        );
    }
}