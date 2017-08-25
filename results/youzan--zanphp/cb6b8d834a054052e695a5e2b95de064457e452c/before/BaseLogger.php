<?php
/**
 * Created by IntelliJ IDEA.
 * User: nuomi
 * Date: 16/5/24
 * Time: 下午2:31
 */

namespace Zan\Framework\Sdk\Log;

use Psr\Log\LoggerInterface;
use Psr\Log\LogLevel;
use Zan\Framework\Foundation\Exception\System\InvalidArgumentException;

abstract class BaseLogger implements LoggerInterface
{
    protected $logMap = [
        'all' => 0,
        'debug' => 1,
        'info' => 2,
        'notice' => 3,
        'warning' => 4,
        'error' => 5,
        'critical' => 6,
        'alert' => 7,
        'emergency' => 8,
    ];
    protected $config;
    protected $writer = null;
    protected $levelNum = 0;
    private $bufferData;

    abstract public function init();

    abstract public function format($level, $message, $context);

    public function __construct(array $config)
    {
        if (!$config) {
            throw new InvalidArgumentException('Config is required' . $config);
            return false;
        }
        $this->config = $config;
        $this->levelNum = $this->getLevelNum($this->config['level']);

    }

    /**
     * System is unusable.
     *
     * @param string $message
     * @param array $context
     * @return null
     */
    public function emergency($message, array $context = array())
    {
        if ($this->checkLevel(LogLevel::EMERGENCY)) {
            yield $this->write(LogLevel::EMERGENCY, $message, $context);
            return false;
        }
        yield null;
    }

    /**
     * Action must be taken immediately.
     *
     * Example: Entire website down, database unavailable, etc. This should
     * trigger the SMS alerts and wake you up.
     *
     * @param string $message
     * @param array $context
     * @return null
     */
    public function alert($message, array $context = array())
    {
        if ($this->checkLevel(LogLevel::ALERT)) {
            yield $this->write(LogLevel::ALERT, $message, $context);
            return false;
        }
        yield null;
    }

    /**
     * Critical conditions.
     *
     * Example: Application component unavailable, unexpected exception.
     *
     * @param string $message
     * @param array $context
     * @return null
     */
    public function critical($message, array $context = array())
    {
        if ($this->checkLevel(LogLevel::CRITICAL)) {
            yield $this->write(LogLevel::CRITICAL, $message, $context);
            return false;
        }
        yield null;
    }

    /**
     * Runtime errors that do not require immediate action but should typically
     * be logged and monitored.
     *
     * @param string $message
     * @param array $context
     * @return null
     */
    public function error($message, array $context = array())
    {
        if ($this->checkLevel(LogLevel::ERROR)) {
            yield $this->write(LogLevel::ERROR, $message, $context);
            return false;
        }
        yield null;
    }

    /**
     * Exceptional occurrences that are not errors.
     *
     * Example: Use of deprecated APIs, poor use of an API, undesirable things
     * that are not necessarily wrong.
     *
     * @param string $message
     * @param array $context
     * @return null
     */
    public function warning($message, array $context = array())
    {
        if ($this->checkLevel(LogLevel::WARNING)) {
            yield $this->write(LogLevel::WARNING, $message, $context);
            return false;
        }
        yield null;
    }

    /**
     * Normal but significant events.
     *
     * @param string $message
     * @param array $context
     * @return null
     */
    public function notice($message, array $context = array())
    {
        if ($this->checkLevel(LogLevel::NOTICE)) {
            yield $this->write(LogLevel::NOTICE, $message, $context);
            return false;
        }
        yield null;
    }

    /**
     * Interesting events.
     *
     * Example: User logs in, SQL logs.
     *
     * @param string $message
     * @param array $context
     * @return null
     */
    public function info($message, array $context = array())
    {
        if ($this->checkLevel(LogLevel::INFO)) {
            yield $this->write(LogLevel::INFO, $message, $context);
            return false;
        }
        yield null;
    }

    /**
     * Detailed debug information.
     *
     * @param string $message
     * @param array $context
     * @return null
     */
    public function debug($message, array $context = array())
    {
        if ($this->checkLevel(LogLevel::DEBUG)) {
            yield $this->write(LogLevel::DEBUG, $message, $context);
            return false;
        }
        yield null;
    }

    /**
     * Logs with an arbitrary level.
     *
     * @param mixed $level
     * @param string $message
     * @param array $context
     * @return null
     * @throws InvalidArgumentException
     */
    public function log($level, $message, array $context = array())
    {
        if (!isset($this->logMap[$level])) {
            throw new InvalidArgumentException('Log level[' . $level . '] is illegal');
            return false;
        }
        yield $this[$level]($message, $context);
    }

    /**
     * @param $level
     * @return int
     */
    protected function getLevelNum($level)
    {
        return $this->logMap[$level];
    }

    public function checkLevel($level)
    {
        $levelNum = $this->getLevelNum($level);
        if ($this->levelNum >= $levelNum) {
            return true;
        }
        return false;
    }

    public function getWriter()
    {
        return $this->writer;
    }

    private function checkAsync()
    {
        return $this->config['async'];
    }

    private function checkBuffer()
    {
        $config = $this->config;
        return (!$config['useBuffer'] || (strlen($this->bufferData) >= $config['bufferSize']));
    }

    public function write($level, $message, array $context = array())
    {
        $log = $this->format($level, $message, $context);
        $this->appendToBuffer($log);
        if (!$this->checkAsync() || $this->checkBuffer()) {
            $this->doWrite();
            $this->emptyBuffer();
        }
    }

    private function appendToBuffer($log)
    {
        $this->bufferData .= $log;
    }

    private function emptyBuffer()
    {
        $this->bufferData = '';
    }

    private function getBuffer()
    {
        return $this->bufferData;
    }

    private function doWrite()
    {
        $this->getWriter()->write($this->getBuffer());
    }

}