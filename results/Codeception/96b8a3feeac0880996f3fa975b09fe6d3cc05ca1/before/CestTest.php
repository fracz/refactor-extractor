<?php

/**
 * Class CestTest
 */
class CestTest extends Codeception\TestCase\Test
{

    /**
     * @group core
     * @group load
     */
    public function testFilename()
    {
        $cest = \Codeception\Util\Stub::make('\Codeception\TestCase\Cest', array(
                'getTestClass' => new \Codeception\Util\Locator(),
                'getTestMethod' => 'combine'
        ));
        $this->assertEquals('Codeception\Util\Locator::combine', $cest->getSignature());
    }

    /**
     * @group core
     */
    public function testCestNamings()
    {
        $cept = new \Codeception\TestCase\Cest();
        $klass = new stdClass();
        $cept->config('testClassInstance',$klass)
            ->config('testMethod', 'user')
            ->config('testFile', 'tests/acceptance/LoginCest.php');

        $this->assertEquals(
            'tests/acceptance/LoginCest.php:user',
            Codeception\TestDescriptor::getTestFullName($cept)
        );
        $this->assertEquals(
            'tests/acceptance/LoginCest.php',
            Codeception\TestDescriptor::getTestFileName($cept)
        );
        $this->assertEquals(
            'stdClass::user',
            Codeception\TestDescriptor::getTestSignature($cept)
        );
    }

}