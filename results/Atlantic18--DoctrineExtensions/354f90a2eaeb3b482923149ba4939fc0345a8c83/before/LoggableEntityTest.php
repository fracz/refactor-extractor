<?php

namespace Gedmo\Loggable;

use Doctrine\Common\Util\Debug,
    Loggable\Fixture\Entity\Article,
    Loggable\Fixture\Entity\Comment;

/**
 * These are tests for loggable behavior
 *
 * @author Gediminas Morkevicius <gediminas.morkevicius@gmail.com>
 * @package Gedmo.Loggable
 * @link http://www.gediminasm.org
 * @license MIT License (http://www.opensource.org/licenses/mit-license.php)
 */
class LoggableEntityTest extends \PHPUnit_Framework_TestCase
{
    const TEST_ENTITY_CLASS_ARTICLE = 'Loggable\Fixture\Entity\Article';
    const TEST_ENTITY_CLASS_COMMENT = 'Loggable\Fixture\Entity\Comment';

    private $articleId;
    private $LoggableListener;
    private $em;

    public function setUp()
    {
        $config = new \Doctrine\ORM\Configuration();
        $config->setMetadataCacheImpl(new \Doctrine\Common\Cache\ArrayCache);
        $config->setQueryCacheImpl(new \Doctrine\Common\Cache\ArrayCache);
        $config->setProxyDir(__DIR__ . '/Proxy');
        $config->setProxyNamespace('Gedmo\Loggable\Proxies');
        $config->setMetadataDriverImpl($config->newDefaultAnnotationDriver());

        $conn = array(
            'driver' => 'pdo_sqlite',
            'memory' => true,
        );

        //$config->setSQLLogger(new \Doctrine\DBAL\Logging\EchoSQLLogger());

        $evm = new \Doctrine\Common\EventManager();
        $this->LoggableListener = new ORM\LoggableListener();
        $this->LoggableListener->setUser('jules');
        $evm->addEventSubscriber($this->LoggableListener);
        $this->em = \Doctrine\ORM\EntityManager::create($conn, $config, $evm);

        $schemaTool = new \Doctrine\ORM\Tools\SchemaTool($this->em);
        $schemaTool->dropSchema(array());
        $schemaTool->createSchema(array(
            $this->em->getClassMetadata(self::TEST_ENTITY_CLASS_ARTICLE),
            $this->em->getClassMetadata(self::TEST_ENTITY_CLASS_COMMENT),
            $this->em->getClassMetadata('Gedmo\Loggable\Entity\HistoryLog'),
        ));

        $this->clearLogs();
    }

    public function testLoggableAllActions()
    {
        $repo = $this->em->getRepository('Gedmo\Loggable\Entity\HistoryLog');

        $this->assertEquals(0, count($repo->findAll()));

        $art0 = new Article();
        $art0->setTitle('My Title');

        $this->em->persist($art0);
        $this->em->flush();

        $log = $repo->findOneBy(array());

        $this->assertNotEquals(null, $log);
        $this->assertEquals('create', $log->getAction());
        $this->assertEquals((string) $art0, $log->getObject());
        $this->assertEquals('jules', $log->getUser());

        $this->clearLogs();
    }

    public function testLoggableNotAllowedAction()
    {
        $repo = $this->em->getRepository('Gedmo\Loggable\Entity\HistoryLog');

        $comment = new Comment();
        $comment->setTitle('My Title');

        $this->em->persist($comment);
        $this->em->flush();

        $this->assertEquals(1, count($repo->findAll()));
        $this->clearLogs();

        $this->em->remove($comment);
        $this->em->flush();

        $this->assertEquals(0, count($repo->findAll()));
    }

    private function clearLogs()
    {
        $meta = $this->em->getClassMetadata('Gedmo\Loggable\Entity\HistoryLog');
        $this->em->getConnection()->delete($meta->getTableName(), array('object' => 'My Title'));
    }
}