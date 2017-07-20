<?php

/*
 * This file is part of Rocketeer
 *
 * (c) Maxime Fabre <ehtnam6@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Rocketeer\Services\Config\Files;

use Symfony\Component\Config\ConfigCache;
use Symfony\Component\Config\Resource\ResourceInterface;

class ConfigurationCache extends ConfigCache
{
    /**
     * Writes cache.
     *
     * @param array               $content  The content to write in the cache
     * @param ResourceInterface[] $metadata An array of ResourceInterface instances
     *
     * @throws \RuntimeException When cache file can't be written
     */
    public function write($content, array $metadata = null)
    {
        parent::write(serialize($content), $metadata);
    }

    /**
     * Flush the cache.
     */
    public function flush()
    {
        @unlink($this->getPath());
    }

    /**
     * Get the contents of the cache.
     *
     * @return array
     */
    public function getContents()
    {
        $file = $this->getPath();

        // Get an unserialize
        $configuration = file_get_contents($file);
        $configuration = unserialize($configuration);

        return $configuration;
    }
}