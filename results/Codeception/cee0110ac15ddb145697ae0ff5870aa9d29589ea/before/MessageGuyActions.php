<?php  //[STAMP] e8393e0d724e9dad80f42426001a9efb
namespace _generated;

// This class was automatically generated by build task
// You should not change it manually as it will be overwritten on next build
// @codingStandardsIgnoreFile

use Codeception\Module\MessageHelper;

trait MessageGuyActions
{

    /**
     * [!] Method is generated. Documentation taken from corresponding module.
     *
     *
     * @see \Codeception\Module\MessageHelper::getMessage()
     */
    public function getMessage($name) {
        return $this->scenario->runStep(new \Codeception\Step\Action('getMessage', func_get_args()));
    }
}