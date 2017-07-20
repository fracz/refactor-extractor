<?php

namespace Wallabag\ImportBundle\Tests\Import;

use Wallabag\UserBundle\Entity\User;
use Wallabag\ImportBundle\Import\WallabagV1Import;
use Monolog\Logger;
use Monolog\Handler\TestHandler;

class WallabagV1ImportTest extends \PHPUnit_Framework_TestCase
{
    protected $user;
    protected $em;
    protected $logHandler;

    private function getWallabagV1Import($unsetUser = false)
    {
        $this->user = new User();

        $this->em = $this->getMockBuilder('Doctrine\ORM\EntityManager')
            ->disableOriginalConstructor()
            ->getMock();

        $pocket = new WallabagV1Import($this->em);

        $this->logHandler = new TestHandler();
        $logger = new Logger('test', array($this->logHandler));
        $pocket->setLogger($logger);

        if (false === $unsetUser) {
            $pocket->setUser($this->user);
        }

        return $pocket;
    }

    public function testInit()
    {
        $wallabagV1Import = $this->getWallabagV1Import();

        $this->assertEquals('Wallabag v1', $wallabagV1Import->getName());
        $this->assertEquals('This importer will import all your wallabag v1 articles.', $wallabagV1Import->getDescription());
    }

    public function testImport()
    {
        $wallabagV1Import = $this->getWallabagV1Import();
        $wallabagV1Import->setFilepath(__DIR__.'/../fixtures/wallabag-v1.json');

        $entryRepo = $this->getMockBuilder('Wallabag\CoreBundle\Repository\EntryRepository')
            ->disableOriginalConstructor()
            ->getMock();

        $entryRepo->expects($this->exactly(3))
            ->method('existByUrlAndUserId')
            ->will($this->onConsecutiveCalls(false, true, false));

        $this->em
            ->expects($this->any())
            ->method('getRepository')
            ->willReturn($entryRepo);

        $res = $wallabagV1Import->import();

        $this->assertTrue($res);
        $this->assertEquals(['skipped' => 1, 'imported' => 2], $wallabagV1Import->getSummary());
    }

    public function testImportBadFile()
    {
        $wallabagV1Import = $this->getWallabagV1Import();
        $wallabagV1Import->setFilepath(__DIR__.'/../fixtures/wallabag-v1.jsonx');

        $res = $wallabagV1Import->import();

        $this->assertFalse($res);

        $records = $this->logHandler->getRecords();
        $this->assertContains('WallabagV1Import: unable to read file', $records[0]['message']);
        $this->assertEquals('ERROR', $records[0]['level_name']);
    }

    public function testImportUserNotDefined()
    {
        $wallabagV1Import = $this->getWallabagV1Import(true);
        $wallabagV1Import->setFilepath(__DIR__.'/../fixtures/wallabag-v1.json');

        $res = $wallabagV1Import->import();

        $this->assertFalse($res);

        $records = $this->logHandler->getRecords();
        $this->assertContains('WallabagV1Import: user is not defined', $records[0]['message']);
        $this->assertEquals('ERROR', $records[0]['level_name']);
    }
}