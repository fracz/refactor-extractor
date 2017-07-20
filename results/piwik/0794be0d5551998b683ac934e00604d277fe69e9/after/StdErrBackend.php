<?php
/**
 * Piwik - free/libre analytics platform
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 */

namespace Piwik\Log\Backend;

use Piwik\Log;
use Piwik\Log\Formatter\Formatter;

/**
 * Writes log to stderr.
 */
class StdErrBackend extends Backend
{
    /**
     * @var bool
     */
    private $isLoggingToStdOut;

    public function __construct(Formatter $formatter, $isLoggingToStdOut)
    {
        $this->isLoggingToStdOut = $isLoggingToStdOut;

        parent::__construct($formatter);
    }

    public function __invoke(array $record, Log $logger)
    {
        if ($record['level'] != Log::ERROR) {
            return;
        }

        $message = $this->formatMessage($record, $logger) . "\n";

        // Do not log on stderr during tests (prevent display of errors in CI output)
        if (! defined('PIWIK_TEST_MODE')) {
            $this->writeToStdErr($message);
        }

        // This is the result of an old hack, I guess to force the error message to be displayed in case of errors
        // TODO we should get rid of it somehow
        if (! $this->isLoggingToStdOut) {
            echo $message;
        }
    }

    private function writeToStdErr($message)
    {
        $fe = fopen('php://stderr', 'w');
        fwrite($fe, $message);
    }
}