<?php
namespace Codeception\Lib\TestFeature;

trait CodeCoverage
{
    /**
     * @return \PHPUnit_Framework_TestResult
     */
    abstract public function getTestResultObject();

    public function codeCoverageStart()
    {
        $codeCoverage = $this->getTestResultObject()->getCodeCoverage();
        if (!$codeCoverage) {
            return;
        }
        $codeCoverage->start($this);
    }

    public function codeCoverageEnd($status, $time)
    {
        $codeCoverage = $this->getTestResultObject()->getCodeCoverage();
        if (!$codeCoverage) {
            return;
        }
        $linesToBeCovered = \PHPUnit_Util_Test::getLinesToBeCovered(get_class($this->testClassInstance), 'test');
        $linesToBeUsed = \PHPUnit_Util_Test::getLinesToBeUsed(get_class($this->testClassInstance), 'test');

        try {
            $codeCoverage->stop(true, $linesToBeCovered, $linesToBeUsed);
        } catch (\PHP_CodeCoverage_Exception $cce) {
            if ($status === \Codeception\Test::STATUS_OK) {
                $this->getTestResultObject()->addError($this, $cce, $time);
            }
        }

    }


}