<?php namespace Spatie\LaravelAnalytics;

use Illuminate\Support\ServiceProvider;
use Google_Client;
use Config;
use Spatie\LaravelAnalytics\Helpers\GoogleApiHelper;

class LaravelAnalyticsServiceProvider extends ServiceProvider
{
    /**
     * Bootstrap the application events.
     */
    public function boot()
    {
        $this->publishes([
            __DIR__.'/config/laravel-analytics.php' =>  config_path('laravel-analytics.php'),
        ]);
    }

    /**
     * Register the service provider.
     */
    public function register()
    {
        $this->app->bind('laravelAnalytics', function ($app) {
            $googleApiHelper = $this->getGoogleApiHelperClient();

            $laravelAnalytics = new LaravelAnalytics($googleApiHelper, Config::get('laravel-analytics.siteId'), Config::get('laravel-analytics.cacheLifetime'));

            return $laravelAnalytics;
        }, true);
    }

    /**
     * Return a GoogleApiHelper with given configuration.
     *
     * @return GoogleApiHelper
     *
     * @throws \Exception
     */
    protected function getGoogleApiHelperClient()
    {
        $this->guardAgainstMissingP12();

        $config = $this->getGoogleClientConfig();

        $client = new Google_Client($config);

        $client->setAccessType('offline');

        $this->configureCredentials($client);

        return new GoogleApiHelper($client);
    }

    /**
     * Throw exception if .p12 file is not present in specified folder.
     *
     * @throws \Exception
     */
    protected function guardAgainstMissingP12()
    {
        if (!\File::exists(Config::get('laravel-analytics.certificatePath'))) {
            throw new \Exception("Can't find the .p12 certificate in: ".Config::get('laravel-analytics.certificatePath'));
        }
    }

    /**
     * Prepare the configuration with published config values.
     *
     * @return array
     */
    protected function getGoogleClientConfig()
    {
        return [
            'oauth2_client_id' => Config::get('laravel-analytics.clientId'),
            'use_objects' => true,
        ];
    }

    /**
     * Set the Google Analytics credentials with published config values.
     *
     * @param Google_Client $client
     *
     * @return Google_Client
     */
    protected function configureCredentials(Google_Client $client)
    {
        $client->setAssertionCredentials(
            new \Google_Auth_AssertionCredentials(
                Config::get('laravel-analytics.serviceEmail'),
                ['https://www.googleapis.com/auth/analytics.readonly'],
                file_get_contents(Config::get('laravel-analytics.certificatePath'))
            )
        );
    }
}