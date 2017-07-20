<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 *
 * @category Piwik
 * @package Piwik
 */
namespace Piwik;

use Piwik\Common;
use Piwik\Db;

/* TODO:
; all calls to the API (method name, parameters, execution time, caller IP, etc.)
; disabled by default as it can cause serious overhead and should only be used wisely
;logger_api_call[] = file
*/

/**
 * TODO
 */
class Log
{
    // log levels
    const NONE = 0;
    const ERROR = 1;
    const WARN = 2;
    const INFO = 3;
    const DEBUG = 4;
    const VERBOSE = 5;

    // config option names
    const LOG_LEVEL_CONFIG_OPTION = 'log_level';
    const LOG_WRITERS_CONFIG_OPTION = 'log_writers';
    const LOGGER_FILE_PATH_CONFIG_OPTION = 'logger_file_path';
    const LOGGER_DATABASE_TABLE_CONFIG_OPTION = 'logger_db_table';

    /**
     * TODO
     */
    private static $instance = null;

    /**
     * TODO
     */
    public static function getInstance()
    {
        if (self::$instance === null) {
            self::$instance = new Log();
        }
        return self::$instance;
    }

    /**
     * TODO
     */
    private $currentLogLevel = self::WARN;

    /**
     * TODO
     */
    private $writers = array();

    /**
     * TODO
     */
    private $logMessageFormat = "[%pluginName%:%datetime%] %message%";

    /**
     * TODO
     */
    private $logToDatabaseTable = "logger_message";

    /**
     * TODO
     */
    private $logToFileFilename;

    /**
     * TODO
     */
    public function __construct()
    {
        $logConfig = Config::getInstance()->log;
        $this->setCurrentLogLevelFromConfig($logConfig);
        $this->setLogWritersFromConfig($logConfig);
        $this->setLogFilePathFromConfig($logConfig);
        $this->setLogDatabaseTableFromConfig($logConfig);
        $this->disableLoggingBasedOnConfig($logConfig);
    }

    private function setLogWritersFromConfig($logConfig)
    {
        // set the log writers
        $logWriters = $logConfig[self::LOG_WRITERS_CONFIG_OPTION];

        $logWriters = array_map('trim', $logWriters);
        foreach ($logWriters as $writerName) {
            $writer = $this->createWriterByName($writerName);
            if (!empty($writer)) {
                $this->writers[] = $writer;
            }
        }
    }

    private function setCurrentLogLevelFromConfig($logConfig)
    {
        if (!empty($logConfig[self::LOG_LEVEL_CONFIG_OPTION])) {
            $logLevel = $this->getLogLevelFromStringName(self::LOG_LEVEL_CONFIG_OPTION);

            if ($logLevel >= self::NONE // sanity check
                && $logLevel <= self::VERBOSE
            ) {
                $this->currentLogLevel = $logLevel;
            }
        }
    }

    private function setLogDatabaseTableFromConfig($logConfig)
    {
        if (!empty($logConfig[self::LOGGER_DATABASE_TABLE_CONFIG_OPTION])) {
            $this->logToDatabaseTable = $logConfig[self::LOGGER_DATABASE_TABLE_CONFIG_OPTION];
        }
    }

    private function setLogFilePathFromConfig($logConfig)
    {
        $logDir = $logConfig[self::LOGGER_FILE_PATH_CONFIG_OPTION];
        if ($logDir[0] != '/' && $logDir[0] != DIRECTORY_SEPARATOR) {
            $logDir = PIWIK_USER_PATH . '/' . $logDir;
        }
        $this->logToFileFilename = $logDir . '/piwik.log';
    }

    private function createWriterByName($writerName)
    {
        $self = $this;

        $writer = false;
        if ($writerName == 'file') {
            $writer = function ($pluginName, $datetime, $message) use ($self) {
                $self->logToFile($this->formatMessage($pluginName, $datetime, $message));
            };
        } else if ($writerName == 'screen') {
            $writer = function ($pluginName, $datetime, $message) use ($self) {
                $self->logToScreen($this->formatMessage($pluginName, $datetime, $message));
            };
        } else if ($writerName == 'db') {
            $writer = function ($pluginName, $datetime, $message) use ($self) {
                $self->logToDatabase($pluginName, $datetime, $message);
            };
        }
        return $writer;
    }

    private function logToFile($message)
    {
        file_put_contents($this->logToFileFilename, $message . "\n", FILE_APPEND);
    }

    private function logToScreen($message)
    {
        echo $message . "\n";
    }

    private function logToDatabase($pluginName, $datetime, $message)
    {
        $sql = "INSERT INTO " . Common::prefixTable($this->logToDatabaseTable)
             . " (plugin, time, message)"
             . " VALUES (?, ?, ?)";
        Db::query($sql, array($pluginName, $datetime, $message));
    }

    /**
     * TODO
     */
    public function log($level, $pluginName, $message, $sprintfParams = array())
    {
        if ($this->shouldLoggerLog($level)) {
            $datetime = date("Y-m-d H:i:s");
            $message = sprintf($message, $sprintfParams);

            $this->writeMessage($pluginName, $message, $datetime);
        }
    }

    /**
     * TODO
     */
    private function formatMessage($pluginName, $message, $datetime)
    {
        return str_replace(
            array("%pluginName%", "%message%", "%datetime%"),
            array($pluginName, $message, $datetime),
            $this->logMessageFormat
        );
    }

    /**
     * TODO
     */
    private function writeMessage($pluginName, $datetime, $message)
    {
        foreach ($this->writers as $writer) {
            $writer($pluginName, $datetime, $message);
        }
    }

    /**
     * TODO
     */
    public static function log($level, $pluginName, $message, $sprintfParams = array())
    {
        self::getInstance()->log($level, $pluginName, $message, $sprintfParams);
    }

    /**
     * TODO
     */
    public static function e($pluginName, $message, $sprintfParams = array())
    {
        self::log(self::ERROR, $pluginName, $message, $sprintfParams);
    }

    /**
     * TODO
     */
    public static function w($pluginName, $message, $sprintfParams = array())
    {
        self::log(self::WARN, $pluginName, $message, $sprintfParams);
    }

    /**
     * TODO
     */
    public static function i($pluginName, $message, $sprintfParams = array())
    {
        self::log(self::INFO, $pluginName, $message, $sprintfParams);
    }

    /**
     * TODO
     */
    public static function d($pluginName, $message, $sprintfParams = array())
    {
        self::log(self::DEBUG, $pluginName, $message, $sprintfParams);
    }

    /**
     * TODO
     */
    public static function v($pluginName, $message, $sprintfParams = array())
    {
        self::log(self::VERBOSE, $pluginName, $message, $sprintfParams);
    }

    /**
     * Returns if logging should work
     * @return bool
     */
    private function shouldLoggerLog($level)
    {
        return $level <= $this->currentLogLevel;
    }

    private function disableLoggingBasedOnConfig($logConfig)
    {
        $disableLogging = false;

        if (!empty($logConfig['log_only_when_cli'])
            && !Common::isPhpCliMode()
        ) {
            $disableLogging = true;
        }

        if (!empty($logConfig['log_only_when_debug_parameter'])
            && !isset($_REQUEST['debug'])
        ) {
            $disableLogging = true;
        }

        if ($disableLogging) {
            $this->currentLogLevel = self::NONE;
        }
    }

    private function getLogLevelFromStringName($name)
    {
        switch ($name) {
            case 'NONE':
                return self::NONE;
            case 'ERROR':
                return self::ERROR;
            case 'WARN':
                return self::WARN;
            case 'INFO':
                return self::INFO;
            case 'DEBUG':
                return self::DEBUG;
            case 'VERBOSE':
                return self::VERBOSE;
            default:
                return -1;
        }
    }
}