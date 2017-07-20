<?php
/**
 * Piwik - free/libre analytics platform
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 */

namespace Piwik\Log\Formatter;

use Monolog\Formatter\FormatterInterface;

/**
 * Formats a log message.
 *
 * Follows the Chain of responsibility design pattern.
 */
abstract class Formatter implements FormatterInterface
{
    /**
     * @var Formatter|null
     */
    protected $next;

    public function __construct(Formatter $next = null)
    {
        $this->next = $next;
    }

    /**
     * {@inheritdoc}
     */
    public function formatBatch(array $records)
    {
        foreach ($records as $key => $record) {
            $records[$key] = $this->format($record);
        }

        return $records;
    }

    /**
     * Chain of responsibility pattern.
     *
     * @param Formatter $formatter
     */
    public function setNext(Formatter $formatter)
    {
        $this->next = $formatter;
    }

    protected function next(array $record)
    {
        if (! $this->next) {
            throw new \RuntimeException('No next formatter to call');
        }

        return $this->next->format($record);
    }
}