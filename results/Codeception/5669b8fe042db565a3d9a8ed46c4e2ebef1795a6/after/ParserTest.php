<?php
use Codeception\Lib\Parser;

/**
 * @group core
 * Class ParserTest
 */
class ParserTest extends \Codeception\Test\TestCase
{
    /**
     * @var Parser
     */
    protected $parser;

    /**
     * @var \Codeception\Scenario
     */
    protected $scenario;

    protected function _before()
    {
        $cept = new \Codeception\Test\Cept('demo','DemoCept.php');

        $this->metadata = $cept->getMetadata();
        $this->scenario = new Codeception\Scenario($cept);
        $this->parser = new Parser($this->scenario, $this->metadata);
    }

    public function testParsingFeature()
    {
        $code = "<?php\n \\\$I->wantTo('run this test'); ";
        $this->parser->parseFeature($code);
        $this->assertEquals('run this test', $this->scenario->getFeature());

        $code = "<?php\n \\\$I->wantToTest('this run'); ";
        $this->parser->parseFeature($code);
        $this->assertEquals('test this run', $this->scenario->getFeature());
    }

    public function testParsingWithWhitespace()
    {
        $code = "<?php\n \\\$I->wantTo( 'run this test' ); ";
        $this->parser->parseFeature($code);
        $this->assertEquals('run this test', $this->scenario->getFeature());
    }

    public function testScenarioOptions()
    {
        $code = <<<EOF
<?php
// @group davert
// @env windows

\$I = new AcceptanceTeser(\$scenario);
EOF;

        $this->parser->parseScenarioOptions($code);
        $this->assertContains('davert', $this->metadata->getGroups());
        $this->assertContains('windows', $this->metadata->getEnv());

    }

    public function testCommentedInBlockScenarioOptions()
    {
        $code = <<<EOF
<?php
/**
 * @skip
 */
EOF;
        $this->parser->parseScenarioOptions($code);
        $this->assertTrue($this->metadata->isBlocked());
    }

    public function testFeatureCommented()
    {
        $code = "<?php\n //\\\$I->wantTo('run this test'); ";
        $this->parser->parseFeature($code);
        $this->assertNull($this->scenario->getFeature());

        $code = "<?php\n /*\n \\\$I->wantTo('run this test'); \n */";
        $this->parser->parseFeature($code);
        $this->assertNull($this->scenario->getFeature());

    }

    public function testScenarioSkipOptionsHandled()
    {
        $code = "<?php\n // @skip pass along";
        $this->parser->parseScenarioOptions($code);
        $this->assertTrue($this->metadata->isBlocked());
    }

    public function testScenarioIncompleteOptionHandled()
    {
        $code = "<?php\n // @incomplete not ready yet";
        $this->parser->parseScenarioOptions($code);
        $this->assertTrue($this->metadata->isBlocked());
    }

    public function testSteps()
    {
        $code = file_get_contents(\Codeception\Configuration::projectDir().'tests/cli/UnitCept.php');
        $this->assertContains('$I->seeInThisFile', $code);
        $this->parser->parseSteps($code);
        $text = $this->scenario->getText();
        $this->assertContains("I see in this file", $text);
    }

    public function testStepsWithFriends()
    {
        $code = file_get_contents(\Codeception\Configuration::projectDir().'tests/web/FriendsCept.php');
        $this->assertContains('$I->haveFriend', $code);
        $this->parser->parseSteps($code);
        $text = $this->scenario->getText();
        $this->assertContains("jon does", $text);
        $this->assertContains("I have friend", $text);
        $this->assertContains("back to me", $text);

    }

    public function testParseFile()
    {
        $classes = Parser::getClassesFromFile(codecept_data_dir('SimpleTest.php'));
        $this->assertEquals(['SampleTest'], $classes);
    }

    public function testParseFileWithClass()
    {
        if (version_compare(PHP_VERSION, '5.5.0', '<')) {
            $this->markTestSkipped('only for php 5.5');
        }
        $classes = Parser::getClassesFromFile(codecept_data_dir('php55Test'));
        $this->assertEquals(['php55Test'], $classes);
    }

    /*
     * https://github.com/Codeception/Codeception/issues/1779
     */
    public function testParseFileWhichUnsetsFileVariable()
    {
        $classes = Parser::getClassesFromFile(codecept_data_dir('unsetFile.php'));
        $this->assertEquals([], $classes);
    }
    /**
     * @group core
     * @throws \Codeception\Exception\TestParseException
     */
    public function testCeptValidation()
    {
        $this->setExpectedException('Codeception\Exception\TestParseException');
        Parser::validate(codecept_data_dir('Invalid.php'));
    }

    /**
     * @group core
     * @throws \Codeception\Exception\TestParseException
     */
    public function testModernValidation()
    {
        if (PHP_MAJOR_VERSION < 7) {
            $this->markTestSkipped();
        }
        $this->setExpectedException('Codeception\Exception\TestParseException');
        Parser::load(codecept_data_dir('Invalid.php'));
    }

    /**
     * @group core
     */
    public function testClassesFromFile()
    {
        $classes = Parser::getClassesFromFile(codecept_data_dir('DummyClass.php'));
        $this->assertContains('DummyClass', $classes);
        $classes = Parser::getClassesFromFile(codecept_data_dir('SimpleWithDependencyInjectionCest.php'));
        $this->assertContains('simpleDI\\LoadedTestWithDependencyInjectionCest', $classes);
        $this->assertContains('simpleDI\\AnotherCest', $classes);
    }
}