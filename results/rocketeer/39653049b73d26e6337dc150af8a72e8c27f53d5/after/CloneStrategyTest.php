<?php
namespace Strategies;

use Rocketeer\TestCases\RocketeerTestCase;

class CloneStrategyTest extends RocketeerTestCase
{
	public function testCanDeployRepository()
	{
		$task = $this->pretendTask('Deploy');
		$task->strategy->deploy();

		$matcher = array(
			'git clone --depth 1 -b master "{repository}" {server}/releases/{release}',
			array(
				"cd {server}/releases/{release}",
				"git submodule update --init --recursive"
			),
		);

		$this->assertHistory($matcher, $task->history->getFlattened());
	}

	public function testCanUpdateRepository()
	{
		$task = $this->pretendTask('Deploy');
		$task->strategy->update();

		$matcher = array(
			array(
				"cd $this->server/releases/20000000000000",
				"git reset --hard",
				"git pull",
			)
		);

		$this->assertHistory($matcher, $task->history->getFlattened());
	}
}