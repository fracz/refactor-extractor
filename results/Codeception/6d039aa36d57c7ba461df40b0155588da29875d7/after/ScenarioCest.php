<?php
use \Codeception\Util\Stub as Stub;

class ScenarioCest
{
    public $class = '\Codeception\Scenario';

    public function run(CodeGuy $I) {

        $I->wantTo('run steps from scenario');
        $I->haveStub($test = Stub::makeEmpty('\Codeception\TestCase\Cept'));
        $I->haveStub($scenario = Stub::make('\Codeception\Scenario', array('test' => $test, 'steps' => Stub::factory('\Codeception\Step', 2))));
        $scenario->run();

        $I->executeTestedMethodOn($scenario);
        $I->seeMethodInvoked($test,'runStep');
        $I->seePropertyEquals($scenario, 'currentStep', 1);
    }

}