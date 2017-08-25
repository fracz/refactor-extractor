<?php namespace BigName\BackupManager\Integrations\Laravel;

use BigName\BackupManager\Databases;
use BigName\BackupManager\Filesystems;
use BigName\BackupManager\Compressors;
use Symfony\Component\Process\Process;
use Illuminate\Support\ServiceProvider;
use BigName\BackupManager\Config\Config;
use BigName\BackupManager\ShellProcessing\ShellProcessor;

/**
 * Class BackupManagerServiceProvider
 * @package BigName\BackupManager\Integrations\Laravel
 */
class BackupManagerServiceProvider extends ServiceProvider
{
    protected $defer = true;

    /**
     * Bootstrap the application events.
     *
     * @return void
     */
    public function boot()
    {
        $this->package('heybigname/backup-manager', 'backup-manager', __DIR__.'/../../..');
    }

    /**
     * Register the service provider.
     *
     * @return void
     */
    public function register()
    {
        $this->registerFilesystemProvider();
        $this->registerDatabaseProvider();
        $this->registerCompressorProvider();
        $this->registerShellProcessor();
        $this->registerArtisanCommands();
    }

    /**
     * Register the filesystem provider.
     *
     * @return void
     */
    private function registerFilesystemProvider()
    {
        $this->app->bind('BigName\BackupManager\Filesystems\FilesystemProvider', function($app) {
            $provider = new Filesystems\FilesystemProvider(new Config($app['config']['backup-manager::storage']));
            $provider->add(new Filesystems\Awss3Filesystem);
            $provider->add(new Filesystems\DropboxFilesystem);
            $provider->add(new Filesystems\FtpFilesystem);
            $provider->add(new Filesystems\LocalFilesystem);
            $provider->add(new Filesystems\RackspaceFilesystem);
            $provider->add(new Filesystems\SftpFilesystem);
            return $provider;
        });
    }

    /**
     * Register the database provider.
     *
     * @return void
     */
    private function registerDatabaseProvider()
    {
        $this->app->bind('BigName\BackupManager\Databases\DatabaseProvider', function($app) {
            $provider = new Databases\DatabaseProvider(new Config($app['config']['backup-manager::database']));
            $provider->add(new Databases\MysqlDatabase);
            $provider->add(new Databases\PostgresqlDatabase);
            return $provider;
        });
    }

    /**
     * Register the compressor provider.
     *
     * @return void
     */
    private function registerCompressorProvider()
    {
        $this->app->bind('BigName\BackupManager\Compressors\CompressorProvider', function() {
            $provider = new Compressors\CompressorProvider;
            $provider->add(new Compressors\GzipCompressor);
            $provider->add(new Compressors\NullCompressor);
            return $provider;
        });
    }

    /**
     * Register the filesystem provider.
     *
     * @return void
     */
    private function registerShellProcessor()
    {
        $this->app->bind('BigName\BackupManager\ShellProcessing\ShellProcessor', function() {
            return new ShellProcessor(new Process(''));
        });
    }

    /**
     * Register the artisan commands.
     *
     * @return void
     */
    private function registerArtisanCommands()
    {
        $this->commands([
            'BigName\BackupManager\Integrations\Laravel\DbBackupCommand',
            'BigName\BackupManager\Integrations\Laravel\DbRestoreCommand',
            'BigName\BackupManager\Integrations\Laravel\DbListCommand',
        ]);
    }

    /**
     * Get the services provided by the provider.
     *
     * @return array
     */
    public function provides()
    {
        return [
            'BigName\BackupManager\Filesystems\FilesystemProvider',
            'BigName\BackupManager\Databases\DatabaseProvider',
            'BigName\BackupManager\ShellProcessing\ShellProcessor',
        ];
    }
}