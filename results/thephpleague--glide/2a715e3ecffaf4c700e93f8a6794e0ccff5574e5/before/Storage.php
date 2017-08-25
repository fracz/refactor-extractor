<?php

namespace Glide;

use Glide\Exceptions\ConfigurationException;
use League\Flysystem\Adapter\Local;
use League\Flysystem\Filesystem;

class Storage
{
    private $storage;

    public function __construct($storage)
    {
        if (!is_string($storage) and !($storage instanceof Filesystem)) {
            throw new ConfigurationException('Not a valid storage parameter.');
        }

        if (is_string($storage)) {
            $storage = new Filesystem(new Local($storage));
        }

        $this->storage = $storage;
    }

    public function get()
    {
        return $this->storage;
    }

    public function has($hash)
    {
        return $this->storage->has($hash);
    }

    public function read($hash)
    {
        return $this->storage->read($hash);
    }

    public function readStream($hash)
    {
        $stream = $this->storage->readStream($hash);
        rewind($stream);
        fpassthru($stream);
        fclose($stream);
    }

    public function getSize($hash)
    {
        return $this->storage->getSize($hash);
    }

    public function getMimetype($hash)
    {
        return $this->storage->getMimetype($hash);
    }

    public function write($hash, $content)
    {
        return $this->storage->write($hash, $content);
    }

    public function delete($hash)
    {
        return $this->storage->delete($hash);
    }
}