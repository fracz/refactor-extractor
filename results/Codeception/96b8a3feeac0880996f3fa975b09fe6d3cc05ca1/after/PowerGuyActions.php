<?php  //[STAMP] 2e4f268b607a01468dde1204faf7221e
namespace _generated;

// This class was automatically generated by build task
// You should not change it manually as it will be overwritten on next build
// @codingStandardsIgnoreFile

trait PowerGuyActions
{
    /**
     * @return \Codeception\Scenario
     */
    abstract protected function getScenario();


    /**
     * [!] Method is generated. Documentation taken from corresponding module.
     *
     *
     * @see \Codeception\Module\PowerHelper::gotThePower()
     */
    public function gotThePower() {
        return $this->getScenario()->runStep(new \Codeception\Step\Action('gotThePower', func_get_args()));
    }


    /**
     * [!] Method is generated. Documentation taken from corresponding module.
     *
     *
     * @see \Codeception\Module\PowerHelper::castFireball()
     */
    public function castFireball() {
        return $this->getScenario()->runStep(new \Codeception\Step\Action('castFireball', func_get_args()));
    }
}