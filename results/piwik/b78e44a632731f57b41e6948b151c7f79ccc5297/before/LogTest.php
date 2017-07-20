<?php
/**
 * Piwik - free/libre analytics platform
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 */

namespace Piwik\Tests\Integration;

use Exception;
use Piwik\Common;
use Piwik\Config;
use Piwik\Container\ContainerFactory;
use Piwik\Container\StaticContainer;
use Piwik\Db;
use Piwik\Error;
use Piwik\ExceptionHandler;
use Piwik\Log;
use Piwik\Plugins\TestPlugin\TestLoggingUtility;
use Piwik\Tests\Framework\TestCase\IntegrationTestCase;

require_once PIWIK_INCLUDE_PATH . '/tests/resources/TestPluginLogClass.php';

/**
 * @group Core
 * @group Core_LogTest
 */
class LogTest extends IntegrationTestCase
{
    const TESTMESSAGE = 'test%smessage';
    const STRING_MESSAGE_FORMAT = '[%tag%] %message%';
    const STRING_MESSAGE_FORMAT_SPRINTF = "[%s] %s";

    public static $expectedExceptionOutput = array(
        'screen' => '<div style=\'word-wrap: break-word; border: 3px solid red; padding:4px; width:70%; background-color:#FFFF96;\'>
        <strong>There is an error. Please report the message
        and full backtrace in the <a href=\'?module=Proxy&action=redirect&url=http://forum.piwik.org\' target=\'_blank\'>Piwik forums</a> (please do a Search first as it might have been reported already!).</strong><br /><br/>
        <em>dummy error message</em> in <strong>LogTest.php</strong> on line <strong>174</strong>
<br /><br />Backtrace --&gt;<div style="font-family:Courier;font-size:10pt"><br />
dummy backtrace</div></div>',
        'file' => '[Piwik\Tests\Integration\LogTest] LogTest.php(174): dummy error message
  dummy backtrace',
        'database' => '[Piwik\Tests\Integration\LogTest] LogTest.php(174): dummy error message
dummy backtrace'
    );

    public static $expectedErrorOutput = array(
        'screen' => '<div style=\'word-wrap: break-word; border: 3px solid red; padding:4px; width:70%; background-color:#FFFF96;\'>
        <strong>There is an error. Please report the message
        and full backtrace in the <a href=\'?module=Proxy&action=redirect&url=http://forum.piwik.org\' target=\'_blank\'>Piwik forums</a> (please do a Search first as it might have been reported already!).</strong><br /><br/>
        <em>Unknown error (102) - dummy error string</em> in <strong>dummyerrorfile.php</strong> on line <strong>145</strong>
<br /><br />Backtrace --&gt;<div style="font-family:Courier;font-size:10pt"><br />
dummy backtrace</div></div>',
        'file' => '[Piwik\Tests\Integration\LogTest] dummyerrorfile.php(145): Unknown error (102) - dummy error string
  dummy backtrace',
        'database' => '[Piwik\Tests\Integration\LogTest] dummyerrorfile.php(145): Unknown error (102) - dummy error string
dummy backtrace'
    );

    private $screenOutput;

    public static function setUpBeforeClass()
    {
        parent::setUpBeforeClass();

        Error::setErrorHandler();
        ExceptionHandler::setUp();
    }

    public static function tearDownAfterClass()
    {
        restore_error_handler();
        restore_exception_handler();

        parent::tearDownAfterClass();
    }

    public function setUp()
    {
        parent::setUp();

        // Create the container in the normal environment (because in tests logging is disabled)
        $containerFactory = new ContainerFactory();
        $container = $containerFactory->create();
        StaticContainer::set($container);
        Log::unsetInstance();

        Config::getInstance()->log['string_message_format'] = self::STRING_MESSAGE_FORMAT;
        Config::getInstance()->log['logger_file_path'] = self::getLogFileLocation();
        Config::getInstance()->log['log_level'] = Log::INFO;
        @unlink(self::getLogFileLocation());
        Log::$debugBacktraceForTests = "dummy backtrace";
    }

    public function tearDown()
    {
        parent::tearDown();

        StaticContainer::reset();
        Log::unsetInstance();

        @unlink(self::getLogFileLocation());
        Log::$debugBacktraceForTests = null;
    }

    /**
     * Data provider for every test.
     */
    public function getBackendsToTest()
    {
        return array(
            'screen'   => array('screen'),
            'file'     => array('file'),
            'database' => array('database'),
        );
    }

    /**
     * @dataProvider getBackendsToTest
     */
    public function testLoggingWorksWhenMessageIsString($backend)
    {
        Config::getInstance()->log['log_writers'] = array($backend);

        ob_start();
        Log::warning(self::TESTMESSAGE);
        $this->screenOutput = ob_get_contents();
        ob_end_clean();

        $this->checkBackend($backend, self::TESTMESSAGE, $formatMessage = true, $tag = __CLASS__);
    }

    /**
     * @dataProvider getBackendsToTest
     */
    public function testLoggingWorksWhenMessageIsSprintfString($backend)
    {
        Config::getInstance()->log['log_writers'] = array($backend);

        ob_start();
        Log::warning(self::TESTMESSAGE, " subst ");
        $this->screenOutput = ob_get_contents();
        ob_end_clean();

        $this->checkBackend($backend, sprintf(self::TESTMESSAGE, " subst "), $formatMessage = true, $tag = __CLASS__);
    }

    /**
     * @dataProvider getBackendsToTest
     */
    public function testLoggingWorksWhenMessageIsError($backend)
    {
        Config::getInstance()->log['log_writers'] = array($backend);

        ob_start();
        $error = new \ErrorException("dummy error string", 0, 102, "dummyerrorfile.php", 145);
        Log::error($error);
        $this->screenOutput = ob_get_contents();
        ob_end_clean();

        $this->checkBackend($backend, self::$expectedErrorOutput[$backend], $formatMessage = false, $tag = __CLASS__);
        $this->checkBackend('screen', self::$expectedErrorOutput['screen']); // errors should always written to the screen
    }

    /**
     * @dataProvider getBackendsToTest
     */
    public function testLoggingWorksWhenMessageIsException($backend)
    {
        Config::getInstance()->log['log_writers'] = array($backend);

        ob_start();
        $exception = new Exception("dummy error message");
        Log::error($exception);
        $this->screenOutput = ob_get_contents();
        ob_end_clean();

        $this->checkBackend($backend, self::$expectedExceptionOutput[$backend], $formatMessage = false, $tag = __CLASS__);
        $this->checkBackend('screen', self::$expectedExceptionOutput['screen']); // errors should always written to the screen
    }

    /**
     * @dataProvider getBackendsToTest
     */
    public function testLoggingCorrectlyIdentifiesPlugin($backend)
    {
        Config::getInstance()->log['log_writers'] = array($backend);

        ob_start();
        TestLoggingUtility::doLog(self::TESTMESSAGE);
        $this->screenOutput = ob_get_contents();
        ob_end_clean();

        $this->checkBackend($backend, self::TESTMESSAGE, $formatMessage = true, $tag = 'TestPlugin');
    }

    /**
     * @dataProvider getBackendsToTest
     */
    public function testLogMessagesIgnoredWhenNotWithinLevel($backend)
    {
        Config::getInstance()->log['log_writers'] = array($backend);
        Config::getInstance()->log['log_level'] = 'ERROR';

        ob_start();
        Log::info(self::TESTMESSAGE);
        $this->screenOutput = ob_get_contents();
        ob_end_clean();

        $this->checkNoMessagesLogged($backend);
    }

    /**
     * @dataProvider getBackendsToTest
     */
    public function testLogMessagesAreTrimmed($backend)
    {
        Config::getInstance()->log['log_writers'] = array($backend);

        ob_start();
        TestLoggingUtility::doLog(" \n   ".self::TESTMESSAGE."\n\n\n   \n");
        $this->screenOutput = ob_get_contents();
        ob_end_clean();

        $this->checkBackend($backend, self::TESTMESSAGE, $formatMessage = true, $tag = 'TestPlugin');
    }

    private function checkBackend($backend, $expectedMessage, $formatMessage = false, $tag = false)
    {
        if ($formatMessage) {
            $expectedMessage = sprintf(self::STRING_MESSAGE_FORMAT_SPRINTF, $tag, $expectedMessage);
        }

        // remove version number from message
        $this->screenOutput = str_replace(" (Piwik ". \Piwik\Version::VERSION.")", "", $this->screenOutput);

        if ($backend == 'screen') {
            if ($formatMessage
                && !Common::isPhpCliMode()) {
                $expectedMessage = '<pre>' . $expectedMessage . '</pre>';
            }

            $this->screenOutput = $this->removePathsFromBacktrace($this->screenOutput);

            $this->assertEquals($expectedMessage . "\n", $this->screenOutput, "unexpected output: ".$this->screenOutput);
        } else if ($backend == 'file') {
            $this->assertTrue(file_exists(self::getLogFileLocation()));

            $fileContents = file_get_contents(self::getLogFileLocation());
            $fileContents = $this->removePathsFromBacktrace($fileContents);

            $this->assertEquals($expectedMessage . "\n", $fileContents);
        } else if ($backend == 'database') {
            $count = Db::fetchOne("SELECT COUNT(*) FROM " . Common::prefixTable('logger_message'));
            $this->assertEquals(1, $count);

            $message = Db::fetchOne("SELECT message FROM " . Common::prefixTable('logger_message') . " LIMIT 1");
            $message = $this->removePathsFromBacktrace($message);
            $this->assertEquals($expectedMessage, $message);

            $tagInDb = Db::fetchOne("SELECT tag FROM " . Common::prefixTable('logger_message') . " LIMIT 1");
            if ($tag === false) {
                $this->assertEmpty($tagInDb);
            } else {
                $this->assertEquals($tag, $tagInDb);
            }
        }
    }

    private function checkNoMessagesLogged($backend)
    {
        if ($backend == 'screen') {
            $this->assertEmpty($this->screenOutput, "Output not empty: ".$this->screenOutput);
        } else if ($backend == 'file') {
            $this->assertFalse(file_exists(self::getLogFileLocation()));
        } else if ($backend == 'database') {
            $this->assertEquals(0, Db::fetchOne("SELECT COUNT(*) FROM " . Common::prefixTable('logger_message')));
        }
    }

    private function removePathsFromBacktrace($content)
    {
        return preg_replace_callback("/(?:\/[^\s(<>]+)*\//", function ($matches) {
            if ($matches[0] == '/') {
                return '/';
            } else {
                return '';
            }
        }, $content);
    }

    public static function getLogFileLocation()
    {
        return StaticContainer::getContainer()->get('path.tmp') . '/logs/piwik.test.log';
    }
}