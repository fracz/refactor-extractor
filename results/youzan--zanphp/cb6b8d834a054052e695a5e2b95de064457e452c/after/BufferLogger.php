<?php
/**
 * Created by IntelliJ IDEA.
 * User: nuomi
 * Date: 16/5/25
 * Time: 下午2:19
 */

namespace Zan\Framework\Sdk\Log;

class BufferLogger extends BaseLogger
{

    private $logger;
    private $bufferSize;
    private $bufferData;

    public function __construct($logger, $config)
    {
        if (!$logger) {
            throw new InvalidArgumentException('Logger is required' . $logger);
            return false;
        }
        $this->logger = $logger;
        $this->config = $config;
        $this->bufferSize = $this->config['bufferSize'];
        $this->init();
    }

    public function init()
    {
        $this->writer = new BufferWriter($this->logger, $this->bufferSize);
    }

    public function format($level, $message, $context)
    {
        return $this->logger->format($level, $message, $context);
    }

    protected function doWrite($log)
    {
        $this->appendToBuffer($log);
        if (!$this->checkAsync() || $this->checkBuffer()) {
            $this->getWriter()->write($this->getBuffer());
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

    private function checkAsync()
    {
        return $this->config['async'];
    }

    private function checkBuffer()
    {
        $config = $this->config;
        return (!$config['useBuffer'] || (strlen($this->bufferData) >= $this->bufferSize));
    }

}