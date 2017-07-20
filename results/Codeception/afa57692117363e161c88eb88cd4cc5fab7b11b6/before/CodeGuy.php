<?php //[STAMP] 79d35194e34268d5009b2a975f6b935d

// This class was automatically generated by build task
// You should not change it manually as it will be overwritten on next build
// @codingStandardsIgnoreFile


use Codeception\Module\CodeHelper;
use Codeception\Module\EmulateModuleHelper;

/**
 * Inherited Methods
 * @method void wantToTest($text)
 * @method void wantTo($text)
 * @method void expectTo($prediction)
 * @method void expect($prediction)
 * @method void amGoingTo($argumentation)
 * @method void am($role)
 * @method void lookForwardTo($achieveValue)
 * @method void comment($description)
 * @method void haveFriend($name)
*/
class CodeGuy extends \Codeception\Actor
{

    /**
     * [!] Method is generated. Documentation taken from corresponding module.
     *
     *
     * Conditional Assertion: Test won't be stopped on fail
     * @see \Codeception\Module\EmulateModuleHelper::seeEquals()
     */
    public function canSeeEquals($expected, $actual) {
        return $this->scenario->runStep(new \Codeception\Step\ConditionalAssertion('seeEquals', func_get_args()));
    }
    /**
     * [!] Method is generated. Documentation taken from corresponding module.
     *
     *
     * @see \Codeception\Module\EmulateModuleHelper::seeEquals()
     */
    public function seeEquals($expected, $actual) {
        return $this->scenario->runStep(new \Codeception\Step\Assertion('seeEquals', func_get_args()));
    }


    /**
     * [!] Method is generated. Documentation taken from corresponding module.
     *
     *
     * Conditional Assertion: Test won't be stopped on fail
     * @see \Codeception\Module\EmulateModuleHelper::seeFeaturesEquals()
     */
    public function canSeeFeaturesEquals($expected) {
        return $this->scenario->runStep(new \Codeception\Step\ConditionalAssertion('seeFeaturesEquals', func_get_args()));
    }
    /**
     * [!] Method is generated. Documentation taken from corresponding module.
     *
     *
     * @see \Codeception\Module\EmulateModuleHelper::seeFeaturesEquals()
     */
    public function seeFeaturesEquals($expected) {
        return $this->scenario->runStep(new \Codeception\Step\Assertion('seeFeaturesEquals', func_get_args()));
    }
}