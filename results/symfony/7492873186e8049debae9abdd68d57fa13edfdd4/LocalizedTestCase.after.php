<?php

/*
 * This file is part of the Symfony package.
 *
 * (c) Fabien Potencier <fabien.potencier@symfony-project.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Symfony\Tests\Component\Form;

require_once __DIR__ . '/TestCase.php';

class LocalizedTestCase extends TestCase
{
    protected function setUp()
    {
        parent::setUp();

        if (!extension_loaded('intl')) {
            $this->markTestSkipped('The "intl" extension is not available');
        }
    }
}