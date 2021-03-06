<?php
/**
 * Piwik - free/libre analytics platform
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 *
 */

namespace Piwik\Plugins\LanguagesManager\Commands;

use Piwik\Container\StaticContainer;
use Piwik\Translation\Transifex\API;
use Symfony\Component\Console\Helper\ProgressHelper;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Input\InputOption;
use Symfony\Component\Console\Output\OutputInterface;

/**
 */
class FetchFromTransifex extends TranslationBase
{
    const DOWNLOAD_PATH = '/transifex';

    protected function configure()
    {
        $path = StaticContainer::get('path.tmp') . self::DOWNLOAD_PATH;

        $this->setName('translations:fetch')
             ->setDescription('Fetches translations files from oTrance to ' . $path)
             ->addOption('username', 'u', InputOption::VALUE_OPTIONAL, 'Transifex username')
             ->addOption('password', 'p', InputOption::VALUE_OPTIONAL, 'Transifex password')
             ->addOption('plugin', 'r', InputOption::VALUE_OPTIONAL, 'Plugin to update');
    }

    protected function execute(InputInterface $input, OutputInterface $output)
    {
        $username = $input->getOption('username');
        $password = $input->getOption('password');
        $plugin = $input->getOption('plugin');

        $resource = 'piwik-'. ($plugin ? 'plugin-'.strtolower($plugin) : 'base');

        $output->writeln("Fetching translations from Transifex for resource $resource");

        $transifexApi = new API($username, $password);

        $languages = $transifexApi->getAvailableLanguageCodes();

        /** @var ProgressHelper $progress */
        $progress = $this->getHelperSet()->get('progress');

        $progress->start($output, count($languages));

        foreach ($languages as $language) {
            try {
                $translations = $transifexApi->getTranslations($resource, $language, true);
                file_put_contents($this->getDownloadPath() . DIRECTORY_SEPARATOR . str_replace('_', '-', strtolower($language)) . '.json', $translations);
            } catch (\Exception $e) {
                $output->writeln("Error fetching language file $language: " . $e->getMessage());
            }
            $progress->advance();
        }

        $progress->finish();
        $output->writeln("Finished fetching new language files from Transifex");
    }

    public static function getDownloadPath()
    {
        $path = StaticContainer::get('path.tmp') . self::DOWNLOAD_PATH;

        if (!is_dir($path)) {
            mkdir($path);
        }

        return $path;
    }
}