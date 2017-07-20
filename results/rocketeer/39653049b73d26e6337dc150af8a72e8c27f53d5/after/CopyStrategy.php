<?php
namespace Rocketeer\Strategies;

use Rocketeer\Interfaces\StrategyInterface;

class CopyStrategy extends CloneStrategy implements StrategyInterface
{
	/**
	 * Deploy a new clean copy of the application
	 *
	 * @param string|null $destination
	 *
	 * @return boolean
	 */
	public function deploy($destination = null)
	{
		// Get the previous release, if none clone from scratch
		$previous = $this->releasesManager->getPreviousRelease();
		$previous = $this->releasesManager->getPathToRelease($previous);
		if (!$previous) {
			return parent::deploy($destination);
		}

		// Recompute destination
		if (!$destination) {
			$destination = $this->releasesManager->getCurrentReleasePath();
		}

		// Copy old release into new one
		$this->command->info('Copying previous release "'.$previous.'" in "'.$destination.'"');
		$this->bash->copy($previous, $destination);

		// Update repository
		return $this->update();
	}
}