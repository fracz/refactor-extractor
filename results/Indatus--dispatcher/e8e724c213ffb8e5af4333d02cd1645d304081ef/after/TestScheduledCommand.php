<?php
/**
 * @author Ben Kuhl <bkuhl@indatus.com>
 */

use Mockery as m;
use Indatus\Dispatcher\Drivers\Cron\Scheduler;

class TestScheduledCommand extends TestCase
{
    /**
     * @var Indatus\Dispatcher\ScheduledCommand
     */
    private $scheduledCommand;

    public function setUp()
    {
        parent::setUp();

        $this->scheduledCommand = m::mock('Indatus\Dispatcher\ScheduledCommand[schedule]');

        $this->app->instance('Indatus\Dispatcher\Schedulable', new Scheduler());
    }

    public function tearDown()
    {
        parent::tearDown();
        m::close();
    }

    public function testDefaultUser()
    {
        $this->assertFalse($this->scheduledCommand->user());
    }

    public function testDefaultEnvironment()
    {
        $this->assertEquals('*', $this->scheduledCommand->environment());
    }

}