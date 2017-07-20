<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 * @version $Id$
 */
class NonceTest extends PHPUnit_Framework_TestCase
{
    /**
     * Dataprovider for acceptable origins test
     */
    public function getAcceptableOriginsTestData()
    {
        return array(
            // HTTP_HOST => expected
            array('example.com', array( 'http://example.com', 'https://example.com' )),
            array('example.com:80', array( 'http://example.com', 'https://example.com' )),
            array('example.com:443', array( 'http://example.com', 'https://example.com' )),
            array('example.com:8080', array( 'http://example.com', 'https://example.com', 'http://example.com:8080', 'https://example.com:8080' )),
        );
    }

    /**
     * @dataProvider getAcceptableOriginsTestData
     * @group Core
     * @group Nonce
     */
    public function test_getAcceptableOrigins($host, $expected)
    {
        $_SERVER['HTTP_HOST'] = $host;
        $this->assertEquals( Piwik_Nonce::getAcceptableOrigins(), $expected, $host );
    }
}