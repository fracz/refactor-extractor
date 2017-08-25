<?php

namespace mageekguy\atoum\phar;

use \mageekguy\atoum;

class stub extends atoum\script
{
	protected $help = false;
	protected $infos = false;
	protected $signature = false;
	protected $decompress = false;
	protected $extract = false;

	public function run(atoum\superglobal $superglobal = null)
	{
		if (PHP_SAPI !== 'cli' || realpath($_SERVER['argv'][0]) !== $this->getName())
		{
			require_once(__DIR__ . '/../../scripts/runners/autorunner.php');
		}
		else
		{
			parent::run($superglobal);

			if ($this->help === true)
			{
				$this->help();
			}

			if ($this->infos === true)
			{
				$this->infos();
			}

			if ($this->signature === true)
			{
				$this->signature();
			}

			if ($this->extract !== false)
			{
				$this->extract();
			}
		}

		return $this;
	}

	protected function handleArgument($argument)
	{
		switch ($argument)
		{
			case '-h':
			case '--help':
				$this->help = true;
				break;

			case '-i':
			case '--infos':
				$this->infos = true;
				break;

			case '-s':
			case '--signature':
				$this->signature = true;
				break;

			case '-e':
			case '--extract':
				$this->arguments->next();
				$directory = $this->arguments->current();

				if ($this->arguments->valid() === false || self::isArgument($directory) === true)
				{
					throw new \logicException('Bad usage of ' . $argument . ', do php ' . $this->getName() . ' --help for more informations');
				}

				$this->extract = $directory;
				break;

			default:
				throw new \logicException('Argument \'' . $argument . '\' is unknown');
		}
	}

	protected function help()
	{
		self::writeMessage(sprintf($this->locale->_('Usage: %s [options]'), $this->getName()));
		self::writeMessage(sprintf($this->locale->_('Atoum version %s by %s.'), atoum\test::getVersion(), atoum\test::author));
		self::writeMessage($this->locale->_('Available options are:'));

		$options = array(
			'-h, --help' => $this->locale->_('Display this help'),
			'-i, --infos' => $this->locale->_('Display informations'),
			'-s, --signature' => $this->locale->_('Display phar signature'),
			'-e <dir>, --extract <dir>' => $this->locale->_('Extract all file from phar in <dir>')
		);

		self::writeLabels($options);

		return $this;
	}

	protected function infos()
	{
		self::writeMessage($this->locale->_('Informations:'));

		$phar = new \Phar(__DIR__ . '../..');

		self::writeLabels($phar->getMetadata());
	}

	protected function signature()
	{
		$phar = new \Phar(__DIR__ . '../..');
		$signature = $phar->getSignature();
		self::writeLabel($this->locale->_('Signature'), $signature['hash']);
		return $this;
	}

	protected function extract()
	{
		if (is_dir($this->extract) === false)
		{
			throw new \logicException('Path \'' . $this->extract . '\' is not a directory');
		}

		if (is_writable($this->extract) === false)
		{
			throw new \logicException('Directory \'' . $this->extract . '\' is not writable');
		}

		$phar = new \Phar(\mageekguy\sparkline::directory);
		$phar->extractTo($this->extract);

		return $this;
	}
}

?>