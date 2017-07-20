<?php

namespace Phalcon\Test\Unit\Translate\Adapter\Gettext;

use Phalcon\Test\Module\UnitTest;
use Phalcon\Translate\Adapter\Gettext;

/**
 * \Phalcon\Test\Unit\Translate\Adapter\Gettext\QueryTest
 * Tests the \Phalcon\Translate\Adapter\Gettext component
 *
 * @copyright (c) 2011-2016 Phalcon Team
 * @link      http://www.phalconphp.com
 * @author    Serghei Iakovlev <serghei@phalconphp.com>
 * @package   Phalcon\Test\Unit\Translate\Adapter\Gettext
 *
 * The contents of this file are subject to the New BSD License that is
 * bundled with this package in the file docs/LICENSE.txt
 *
 * If you did not receive a copy of the license and are unable to obtain it
 * through the world-wide-web, please send an email to license@phalconphp.com
 * so that we can send you a copy immediately.
 */
class QueryTest extends UnitTest
{
    /**
     * executed before each test
     */
    public function _before()
    {
        parent::_before();

        if (!extension_loaded('gettext')) {
            $this->markTestSkipped('Warning: gettext extension is not loaded');
        }
    }

    /**
     * Tests the query with Ukranian
     *
     * @author Serghei Iakovlev <serghei@phalconphp.com>
     * @since  2016-01-16
     */
    public function testQueryUkranian()
    {
        $this->specify(
            "The key does not exist with query in Ukranian",
            function () {
                $translator = new Gettext(
                    [
                        'locale'        => 'uk_UA.utf8',
                        'defaultDomain' => 'messages',
                        'directory'     => PATH_DATA . 'translation/gettext'
                    ]
                );

                expect($translator->query('Hello'))->equals('Привіт');
            }
        );
    }
}