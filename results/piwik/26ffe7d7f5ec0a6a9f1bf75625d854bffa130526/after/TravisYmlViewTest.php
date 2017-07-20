<?php
/**
 * Piwik - free/libre analytics platform
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 *
 */
namespace Piwik\Plugins\CoreConsole\tests\Unit;

// DeviceDectector requires Spyc

use Symfony\Component\Console\Output\ConsoleOutput;
use Piwik\Plugins\CoreConsole\TravisYmlView;
use PHPUnit_Framework_TestCase;
use Spyc;

/**
 * @group CoreConsole
 * @group CoreConsole_TravisYmlViewTest
 */
class TravisYmlViewTest extends PHPUnit_Framework_TestCase
{
    public function testViewGeneratesCorrectLookingYAML()
    {
        $view = new TravisYmlView();
        $view->setPlugin('ExamplePlugin');
        $view->setExtraGlobalEnvVars(array('artifactspass', 'githubtoken'));
        $view->setGenerateYmlCommand('./console generate:travis-yml \'arg1\' arg2');
        $output = $view->render();

        $yaml = Spyc::YAMLLoadString($output);

        $this->assertNotEmpty($yaml['env']);
        $this->assertNotEmpty($yaml['env']['global']);
        $this->assertContains("PLUGIN_NAME=ExamplePlugin", $yaml['env']['global']);
        $this->assertContains("PIWIK_ROOT_DIR=\$TRAVIS_BUILD_DIR/piwik", $yaml['env']['global']);
        $this->assertContains(array('secure' => 'artifactspass'), $yaml['env']['global']);
        $this->assertContains(array('secure' => 'githubtoken'), $yaml['env']['global']);

        $this->assertNotEmpty($yaml['env']['matrix']);
        $this->assertContains("TEST_SUITE=PluginTests MYSQL_ADAPTER=PDO_MYSQL", $yaml['env']['matrix']);
        $this->assertContains("TEST_SUITE=PluginTests MYSQL_ADAPTER=PDO_MYSQL TEST_AGAINST_CORE=latest_stable", $yaml['env']['matrix']);
        $this->assertNotContains("TEST_SUITE=UITests MYSQL_ADAPTER=PDO_MYSQL", $yaml['env']['matrix']);

        $this->assertBuildSectionsNotEmpty($yaml);

        $this->assertContains("export GENERATE_TRAVIS_YML_COMMAND='./console generate:travis-yml \\'arg1\\' arg2'", $yaml['before_script']);

        $this->assertViewUsesPluginSpecifiedTravisCommands($yaml);
    }

    public function testViewPreservesCommentsAndEnvVarsIfExistingYml()
    {
        $view = new TravisYmlView();
        $view->setPlugin('ExamplePlugin');
        $view->setExtraGlobalEnvVars(array('artifactspass', 'githubtoken'));
        $view->setGenerateYmlCommand('./console generate:travis-yml arg1 arg2');
        $view->processExistingTravisYml(PIWIK_INCLUDE_PATH . '/plugins/CoreConsole/tests/resources/test.travis.yml');
        $output = $view->render();

        $yaml = Spyc::YAMLLoadString($output);

        $this->assertNotEmpty($yaml['env']);
        $this->assertNotEmpty($yaml['env']['global']);

        $this->assertContains("PRESERVED_VAR=123", $yaml['env']['global']);
        $this->assertContains(array('secure' => 'anotherpreservedvar'), $yaml['env']['global']);
        $this->assertNotContains("PLUGIN_NAME=ExamplePlugin", $yaml['env']['global']);
        $this->assertNotContains("PIWIK_ROOT_DIR=\$TRAVIS_BUILD_DIR/piwik", $yaml['env']['global']);

        $this->assertBuildSectionsNotEmpty($yaml);

        $this->assertNotEmpty($yaml['custom_section']);
        $this->assertContains("custom_section:\n  - this will be preserved\n  # as should this", $output);

        $this->assertNotEmpty($yaml['notifications']);
        $this->assertContains("notifications:\n  # another section\n  - a\n  - b\n  - c", $output);

        $this->assertViewUsesPluginSpecifiedTravisCommands($yaml);
    }

    private function assertBuildSectionsNotEmpty($yaml)
    {
        $this->assertNotEmpty($yaml['before_install']);
        $this->assertNotEmpty($yaml['install']);
        $this->assertNotEmpty($yaml['before_script']);
        $this->assertNotEmpty($yaml['after_script']);
        $this->assertNotEmpty($yaml['after_success']);
    }

    private function assertViewUsesPluginSpecifiedTravisCommands($yaml)
    {
        // TODO
    }
}