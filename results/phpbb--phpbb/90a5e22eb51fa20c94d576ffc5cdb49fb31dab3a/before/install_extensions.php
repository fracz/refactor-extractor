<?php
/**
 *
 * This file is part of the phpBB Forum Software package.
 *
 * @copyright (c) phpBB Limited <https://www.phpbb.com>
 * @license GNU General Public License, version 2 (GPL-2.0)
 *
 * For full copyright and license information, please see
 * the docs/CREDITS.txt file.
 *
 */

namespace phpbb\install\module\install_finish\task;

/**
 * Installs extensions that exist in ext folder upon install
 */
class install_extensions extends \phpbb\install\task_base
{
	/**
	 * @var \phpbb\install\helper\config
	 */
	protected $install_config;

	/**
	 * @var \phpbb\install\helper\iohandler\iohandler_interface
	 */
	protected $iohandler;

	/**
	 * @var \phpbb\config\db
	 */
	protected $config;

	/**
	 * @var \phpbb\log\log_interface
	 */
	protected $log;

	/**
	 * @var \phpbb\user
	 */
	protected $user;

	/** @var \phpbb\extension\manager */
	protected $extension_manager;

	/** @var \Symfony\Component\Finder\Finder */
	protected $finder;

	/**
	 * Constructor
	 *
	 * @param \phpbb\install\helper\container_factory				$container
	 * @param \phpbb\install\helper\config							$install_config
	 * @param \phpbb\install\helper\iohandler\iohandler_interface	$iohandler
	 * @param string												$phpbb_root_path phpBB root path
	 */
	public function __construct(\phpbb\install\helper\container_factory $container, \phpbb\install\helper\config $install_config, \phpbb\install\helper\iohandler\iohandler_interface $iohandler, $phpbb_root_path)
	{
		$this->install_config	= $install_config;
		$this->iohandler		= $iohandler;

		$this->log				= $container->get('log');
		$this->user				= $container->get('user');
		$this->extension_manager = $container->get('ext.manager');
		$this->config			= $container->get('config');
		$this->finder = new \Symfony\Component\Finder\Finder();
		$this->finder->in($phpbb_root_path . 'ext/')
			->ignoreUnreadableDirs()
			->depth('< 3')
			->files()
			->name('composer.json');

		// Make sure asset version exists in config. Otherwise we might try to
		// insert the assets_version setting into the database and cause a
		// duplicate entry error.
		if (!isset($this->config['assets_version']))
		{
			$this->config['assets_version'] = 0;
		}

		parent::__construct(true);
	}

	/**
	 * {@inheritdoc}
	 */
	public function run()
	{
		if (defined('PHPBB_ENVIRONMENT') && PHPBB_ENVIRONMENT === 'test')
		{
			return;
		}

		$this->user->session_begin();
		$this->user->setup(array('common', 'acp/common', 'cli'));

		// Find available extensions
		foreach ($this->finder as $file)
		{
			/** @var \SplFileInfo $file */
			$ext_name = preg_replace('#(.+ext[\\/\\\])#', '', dirname($file->getRealPath()));

			if ($this->extension_manager->is_available($ext_name))
			{
				$this->extension_manager->enable($ext_name);
				$this->extension_manager->load_extensions();

				if (!$this->extension_manager->is_enabled($ext_name))
				{
					// Create log
					$this->log->add('admin', ANONYMOUS, '', 'LOG_EXT_ENABLE', time(), array($ext_name));
				}
			}
			else
			{
				$this->iohandler->add_log_message('CLI_EXTENSION_ENABLE_FAILURE', array($ext_name));
			}
		}
	}

	/**
	 * {@inheritdoc}
	 */
	static public function get_step_count()
	{
		return 0;
	}

	/**
	 * {@inheritdoc}
	 */
	public function get_task_lang_name()
	{
		return 'TASK_INSTALL_EXTENSIONS';
	}
}