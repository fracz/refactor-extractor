<?php

/*
 * This file is part of Rocketeer
 *
 * (c) Maxime Fabre <ehtnam6@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Rocketeer\Tasks;

use Mockery\MockInterface;
use Rocketeer\TestCases\RocketeerTestCase;

class SetupTest extends RocketeerTestCase
{
    public function testCanSetupServer()
    {
        $this->usesComposer();
        $this->pretend();

        $this->mockNoCurrentRelease();

        $this->assertTaskHistory('Setup', [
            'git --version',
            '{php} -m',
            'mkdir {server}/',
            'mkdir -p {server}/releases',
            'mkdir -p {server}/current',
            'mkdir -p {server}/shared',
        ]);
    }

    public function testCanSetupStages()
    {
        $this->usesComposer();
        $this->pretend();
        $this->mockNoCurrentRelease();
        $this->swapConfig([
            'stages.stages' => ['staging', 'production'],
        ]);

        $this->assertTaskHistory('Setup', [
            'git --version',
            '{php} -m',
            'mkdir {server}/',
            'mkdir -p {server}/staging/releases',
            'mkdir -p {server}/staging/current',
            'mkdir -p {server}/staging/shared',
            'mkdir -p {server}/production/releases',
            'mkdir -p {server}/production/current',
            'mkdir -p {server}/production/shared',
        ]);
    }

    public function testRunningSetupKeepsCurrentConfiguredStage()
    {
        $this->usesComposer(true, 'staging');
        $this->pretend();
        $this->mockNoCurrentRelease('staging');
        $this->swapConfig([
            'stages.stages' => ['staging', 'production'],
        ]);

        $this->connections->setStage('staging');
        $this->assertEquals('staging', $this->connections->getCurrentConnection()->stage);
        $this->assertTaskHistory('Setup', [
            'git --version',
            '{php} -m',
            'mkdir {server}/',
            'mkdir -p {server}/staging/releases',
            'mkdir -p {server}/staging/current',
            'mkdir -p {server}/staging/shared',
            'mkdir -p {server}/production/releases',
            'mkdir -p {server}/production/current',
            'mkdir -p {server}/production/shared',
        ], [
            'stage' => 'staging',
        ]);

        $this->assertEquals('staging', $this->connections->getCurrentConnection()->stage);
    }

    protected function mockNoCurrentRelease($stage = null)
    {
        $this->mockReleases(function (MockInterface $mock) use ($stage) {
            return $mock
                ->shouldReceive('getCurrentRelease')->andReturn(null)
                ->shouldReceive('getCurrentReleasePath')->andReturnUsing(function ($path = null) use ($stage) {
                    $stage = $stage ? $stage.'/' : null;

                    return $this->server.'/'.$stage.'releases/20000000000000/'.$path;
                });
        });
    }
}