<?php

use BigName\DatabaseBackup\Filesystems\AwsS3Filesystem;
use Mockery as m;

class AwsS3FilesystemTest extends PHPUnit_Framework_TestCase
{
    protected function tearDown()
    {
        m::close();
    }

    public function test_can_create()
    {
        $s3 = new AwsS3Filesystem();
        $this->assertInstanceOf('BigName\DatabaseBackup\Filesystems\AwsS3Filesystem', $s3);
    }

    public function test_get_correct_filesystem()
    {
        $s3 = new AwsS3Filesystem();
        $filesystem = $s3->get([
            'key' => 'key',
            'secret' => 'secret',
            'region' => Aws\Common\Enum\Region::US_EAST_1,
            'bucket' => 'bucket',
        ]);
        $this->assertInstanceOf('League\Flysystem\Adapter\AwsS3', $filesystem->getAdapter());
    }
}