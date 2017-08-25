<?php declare(strict_types=1);
namespace Phan;

class IssueInstance {

    /** @var Issue */
    private $issue;

    /** @var array */
    private $template_parameters;

    /** @var string */
    private $file;

    /** @var int */
    private $line;

    /**
     * @param Issue $issue
     * @param string $file
     * @param int $line
     * @param array template_parameters
     */
    public function __construct(
        Issue $issue,
        string $file,
        int $line,
        array $template_parameters
    ) {
        $this->issue = $issue;
        $this->template_parameters = $template_parameters;
        $this->file = $file;
        $this->line = $line;
    }

    /**
     * @return Issue
     */
    public function getIssue() : Issue {
        return $this->issue;
    }

    /**
     * @return array
     */
    public function getTemplateParameters() : array {
        return $this->template_parameters;
    }

    /**
     * @return string
     */
    public function getFile() : string {
        return $this->file;
    }

    /**
     * @return int
     */
    public function getLine() : int {
        return $this->line;
    }

    /**
     * @return void
     */
    public function __invoke() {
        Log::err(
            $this->getIssue()->getCategory(),
            $this->getIssue()->getType(),
            $this->getIssue()->getSeverity(),
            call_user_func_array('sprintf', array_merge(
                [ $this->getIssue()->getTemplate() ],
                $this->getTemplateParameters()
            )),
            $this->getFile(),
            $this->getLine()
        );
    }

}