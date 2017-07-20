<?php
/**
 * Piwik - free/libre analytics platform
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 */

namespace Piwik\Log\Backend;

use Monolog\Handler\AbstractProcessingHandler;
use Piwik\Common;
use Piwik\Db;

/**
 * Writes log to database.
 */
class DatabaseBackend extends AbstractProcessingHandler
{
    protected function write(array $record)
    {
        $record = $record['formatted'];

        $sql = "INSERT INTO " . Common::prefixTable('logger_message')
            . " (tag, timestamp, level, message)"
            . " VALUES (?, ?, ?, ?)";

        Db::query($sql, array($record['extra']['class'], $record['datetime']->format('Y-m-d H:i:s'), $record['level_name'], (string) $record['message']));
    }
}