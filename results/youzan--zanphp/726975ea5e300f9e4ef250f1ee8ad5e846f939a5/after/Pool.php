<?php
/**
 * Created by IntelliJ IDEA.
 * User: winglechen
 * Date: 16/4/4
 * Time: 01:20
 */

namespace Zan\Framework\Network\Connection;


use Zan\Framework\Contract\Network\ConnectionFactory;
use Zan\Framework\Contract\Network\ConnectionPool;
use Zan\Framework\Contract\Network\Connection;
use Zan\Framework\Foundation\Core\Event;
use Zan\Framework\Network\Server\Timer\Timer;
use Zan\Framework\Utilities\Types\ObjectArray;
use Zan\Framework\Utilities\Types\Time;
use Zan\Framework\Foundation\Coroutine\Task;

class Pool implements ConnectionPool
{
    private $freeConnection = null;

    private $activeConnection = null;

    private $poolConfig = null;

    private $factory = null;

    private $type = null;

    public $waitNum = 0;

    public function __construct(ConnectionFactory $connectionFactory, array $config, $type)
    {
        $this->poolConfig = $config;
        $this->factory = $connectionFactory;
        $this->type = $type;
        $this->init();
    }

    public function init()
    {
        $initConnection = $this->poolConfig['pool']['init-connection'];
        $min = isset($this->poolConfig['pool']['minimum-connection-count']) ?
            $this->poolConfig['pool']['minimum-connection-count'] : 2;
        if ($initConnection < $min) {
            $initConnection = $min;
        }
        $this->freeConnection = new ObjectArray();
        $this->activeConnection = new ObjectArray();
        for ($i = 0; $i < $initConnection; $i++) {
            //todo 创建链接,存入数组
            $this->createConnect();
        }
    }

    public function createConnect($previousConnectionHash = '')
    {
        $max = isset($this->poolConfig['pool']['maximum-connection-count']) ?
            $this->poolConfig['pool']['maximum-connection-count'] : 30;
        $sumCount = $this->activeConnection->length() + $this->freeConnection->length();
        if($sumCount >= $max) {
            return null;
        }
        $connection = $this->factory->create();
        if  ('' !== $previousConnectionHash) {
            $previousKey = ReconnectionPloy::getInstance()->getReconnectTime($previousConnectionHash);
            if ($this->type == 'Mysqli') {
                if (swoole2x()) {
                    if (!$connection->getSocket()->error){
                        $connection->heartbeat();
                    } else {
                        ReconnectionPloy::getInstance()->setReconnectTime(spl_object_hash($connection),$previousKey);
                        $this->remove($connection);
                    }
                } else {
                    if (!$connection->getSocket()->connect_errno){
                        $connection->heartbeat();
                    } else {
                        ReconnectionPloy::getInstance()->setReconnectTime(spl_object_hash($connection),$previousKey);
                        $this->remove($connection);
                    }
                }
                $connection->setPool($this);
            } else {
                ReconnectionPloy::getInstance()->setReconnectTime(spl_object_hash($connection), $previousKey);
            }
            ReconnectionPloy::getInstance()->connectSuccess($previousConnectionHash);
        }

        if ($connection->getIsAsync()) {
            $this->activeConnection->push($connection);
        } else {
            $this->freeConnection->push($connection);
        }
        if ('' == $previousConnectionHash || $this->type !== 'Mysqli') {
            $connection->setPool($this);
            $connection->heartbeat();
        }
        $connection->setEngine($this->type);
    }

    public function getFreeConnection()
    {
        return $this->freeConnection;
    }

    public function getActiveConnection()
    {
        return $this->activeConnection;
    }

    public function reload(array $config)
    {
    }

    public function get($connection = null)
    {
        if ($this->freeConnection->isEmpty()) {
            $this->createConnect();
        }

        if (null == $connection) {
            $connection = $this->freeConnection->pop();
            if (null != $connection) {
                $this->activeConnection->push($connection);
            }

        } else {
            $this->freeConnection->remove($connection);
            $this->activeConnection->push($connection);
        }
        if (null === $connection) {
            yield null;
            return;
        }
        $connection->setUnReleased();
        $connection->lastUsedTime = Time::current(true);
        yield $connection;
    }

    public function recycle(Connection $conn)
    {
        $evtName = null;

        $this->freeConnection->push($conn);
        $this->activeConnection->remove($conn);

        if (count($this->freeConnection) == 1) {
            $evtName = $this->poolConfig['pool']['pool_name'] . '_free';
            Event::fire($evtName, [], false);
        }
    }

    public function remove(Connection $conn)
    {
        $this->freeConnection->remove($conn);
        $this->activeConnection->remove($conn);
        $connHashCode = spl_object_hash($conn);
        if (null === ReconnectionPloy::getInstance()->getReconnectTime($connHashCode)) {
            ReconnectionPloy::getInstance()->setReconnectTime($connHashCode, 0);
            $this->createConnect($connHashCode);
            return;
        }
        ReconnectionPloy::getInstance()->reconnect($conn, $this);
    }

    /**
     * @return array|null
     */
    public function getPoolConfig()
    {
        return $this->poolConfig;
    }
}