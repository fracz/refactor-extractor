<?php

namespace Gaufrette\Adapter;

use Gaufrette\Adapter;
use Gaufrette\Util;
use Gaufrette\Exception;

/**
 * Dropbox adapter
 *
 * @author Markus Bachmann <markus.bachmann@bachi.biz>
 * @author Antoine Hérault <antoine.herault@gmail.com>
 * @author Leszek Prabucki <leszek.prabucki@gmail.com>
 */
class Dropbox implements Adapter
{
    protected $client;

    /**
     * Constructor
     *
     * @param \Dropbox_API $client The Dropbox API client
     */
    public function __construct(\Dropbox_API $client)
    {
        $this->client = $client;
    }

    /**
     * {@inheritDoc}
     */
    public function read($key)
    {
        try {
            return $this->client->getFile($key);
        } catch (\Exception $e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public function isDirectory($key)
    {
        $metadata = $this->getMetadata($key);

        return (boolean) isset($metadata['is_dir']) ? $metadata['is_dir'] : false;
    }

    /**
     * {@inheritDoc}
     */
    public function write($key, $content)
    {
        $resource = tmpfile();
        fwrite($resource, $content);
        fseek($resource, 0);

        try {
            $this->client->putFile($key, $resource);
        } catch (\Exception $e) {
            fclose($resource);

            return false;
        }

        fclose($resource);

        return Util\Size::fromContent($content);
    }

    /**
     * {@inheritDoc}
     */
    public function delete($key)
    {
        try {
            $this->client->delete($key);
        } catch (\Exception $e) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public function rename($sourceKey, $targetKey)
    {
        try {
            $this->client->move($sourceKey, $targetKey);
        } catch (\Exception $e) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public function mtime($key)
    {
        $metadata = $this->getMetadata($key);

        return strtotime($metadata['modified']);
    }

    /**
     * {@inheritDoc}
     */
    public function keys()
    {
        $metadata = $this->client->getMetaData('/', true);
        $files    = isset($metadata['contents']) ? $metadata['contents'] : array();

        $files = array_map(
            function($value) {
                return ltrim($value['path'], '/');
            },
            $files
        );

        $dirs = array();
        foreach ($files as $file)
        {
            if ('.' !== dirname($file)) {
                $dirs[] = dirname($file);
            }
        }

        $keys = array_merge($files, $dirs);
        sort($keys);

        return $keys;
    }

    /**
     * {@inheritDoc}
     */
    public function exists($key)
    {
        try {
            $this->getMetadata($key);

            return true;
        } catch (Exception\FileNotFound $e) {
            return false;
        }
    }

    private function getMetadata($key)
    {
        try {
            $metadata = $this->client->getMetaData($key, false);
        } catch (\Dropbox_Exception_NotFound $e) {
            throw new Exception\FileNotFound($key, 0, $e);
        }

        // TODO find a way to exclude deleted files
        if (isset($metadata['is_deleted']) && $metadata['is_deleted']) {
            throw new Exception\FileNotFound($key);
        }

        return $metadata;
    }
}