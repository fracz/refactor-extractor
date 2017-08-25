<?php

/**
 * This file is part of Bootstrap CMS by Graham Campbell.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * @package    Bootstrap-CMS
 * @author     Graham Campbell
 * @license    GNU AFFERO GENERAL PUBLIC LICENSE
 * @copyright  Copyright (C) 2013  Graham Campbell
 * @link       https://github.com/GrahamCampbell/Bootstrap-CMS
 */

return array(

    /*
    |--------------------------------------------------------------------------
    | Application Debug Mode
    |--------------------------------------------------------------------------
    |
    | When your application is in debug mode, detailed error messages with
    | stack traces will be shown on every error that occurs within your
    | application. If disabled, a simple generic error page is shown.
    |
    */

    'debug' => false,

    /*
    |--------------------------------------------------------------------------
    | Application URL
    |--------------------------------------------------------------------------
    |
    | This URL is used by the console to properly generate URLs when using
    | the Artisan command line tool. You should set this to the root of
    | your application so that it is used when running Artisan tasks.
    |
    */

    'url' => '',

    /*
    |--------------------------------------------------------------------------
    | Application Timezone
    |--------------------------------------------------------------------------
    |
    | Here you may specify the default timezone for your application, which
    | will be used by the PHP date and date-time functions. We have gone
    | ahead and set this to a sensible default for you out of the box.
    |
    */

    'timezone' => 'UTC',

    /*
    |--------------------------------------------------------------------------
    | Application Locale Configuration
    |--------------------------------------------------------------------------
    |
    | The application locale determines the default locale that will be used
    | by the translation service provider. You are free to set this value
    | to any of the locales which will be supported by the application.
    |
    */

    'locale' => 'en',

    /*
    |--------------------------------------------------------------------------
    | Encryption Key
    |--------------------------------------------------------------------------
    |
    | This key is used by the Illuminate encrypter service and should be set
    | to a random, 32 character string, otherwise these encrypted strings
    | will not be safe. Please do this before deploying an application!
    |
    */

    'key' => 'YourSecretKey!!!',

    /*
    |--------------------------------------------------------------------------
    | Autoloaded Service Providers
    |--------------------------------------------------------------------------
    |
    | The service providers listed here will be automatically loaded on the
    | request to your application. Feel free to add your own services to
    | this array to grant expanded functionality to your applications.
    |
    */

    'providers' => array(

        'Illuminate\Foundation\Providers\ArtisanServiceProvider',
        'Illuminate\Auth\AuthServiceProvider',
        'Illuminate\Cache\CacheServiceProvider',
        'Illuminate\Foundation\Providers\CommandCreatorServiceProvider',
        'Illuminate\Session\CommandsServiceProvider',
        'Illuminate\Foundation\Providers\ComposerServiceProvider',
        'Illuminate\Routing\ControllerServiceProvider',
        'Illuminate\Cookie\CookieServiceProvider',
        'Illuminate\Database\DatabaseServiceProvider',
        'Illuminate\Encryption\EncryptionServiceProvider',
        'Illuminate\Filesystem\FilesystemServiceProvider',
        'Illuminate\Hashing\HashServiceProvider',
        'Illuminate\Html\HtmlServiceProvider',
        'Illuminate\Foundation\Providers\KeyGeneratorServiceProvider',
        'Illuminate\Log\LogServiceProvider',
        'Illuminate\Mail\MailServiceProvider',
        'Illuminate\Foundation\Providers\MaintenanceServiceProvider',
        'Illuminate\Database\MigrationServiceProvider',
        'Illuminate\Foundation\Providers\OptimizeServiceProvider',
        'Illuminate\Pagination\PaginationServiceProvider',
        'Illuminate\Foundation\Providers\PublisherServiceProvider',
        'Illuminate\Queue\QueueServiceProvider',
        'Illuminate\Redis\RedisServiceProvider',
        'Illuminate\Auth\Reminders\ReminderServiceProvider',
        'Illuminate\Foundation\Providers\RouteListServiceProvider',
        'Illuminate\Database\SeedServiceProvider',
        'Illuminate\Foundation\Providers\ServerServiceProvider',
        'Illuminate\Session\SessionServiceProvider',
        'Illuminate\Foundation\Providers\TinkerServiceProvider',
        'Illuminate\Translation\TranslationServiceProvider',
        'Illuminate\Validation\ValidationServiceProvider',
        'Illuminate\View\ViewServiceProvider',
        'Illuminate\Workbench\WorkbenchServiceProvider',
        'Cartalyst\Sentry\SentryServiceProvider',
        'GrahamCampbell\Queuing\QueuingServiceProvider',
        'GrahamCampbell\HTMLMin\HTMLMinServiceProvider',
        'GrahamCampbell\Security\SecurityServiceProvider',
        'GrahamCampbell\Binput\BinputServiceProvider',
        'GrahamCampbell\Passwd\PasswdServiceProvider',
        'GrahamCampbell\Navigation\NavigationServiceProvider',
        'GrahamCampbell\CoreAPI\CoreAPIServiceProvider',
        'GrahamCampbell\CloudFlareAPI\CloudFlareAPIServiceProvider',
        'GrahamCampbell\CMSCore\CMSCoreServiceProvider',
        'DaveJamesMiller\Boris\BorisServiceProvider',
        'VTalbot\Markdown\MarkdownServiceProvider',
        'Fideloper\Proxy\ProxyServiceProvider',
        'Lightgear\Asset\AssetServiceProvider',
        'Kmd\Logviewer\LogviewerServiceProvider',
        'Barryvdh\Debugbar\ServiceProvider',
        'Barryvdh\DomPDF\ServiceProvider'

    ),

    /*
    |--------------------------------------------------------------------------
    | Service Provider Manifest
    |--------------------------------------------------------------------------
    |
    | The service provider manifest is used by Laravel to lazy load service
    | providers which are not needed for each request, as well to keep a
    | list of all of the services. Here, you may set its storage spot.
    |
    */

    'manifest' => storage_path().'/meta',

    /*
    |--------------------------------------------------------------------------
    | Class Aliases
    |--------------------------------------------------------------------------
    |
    | This array of class aliases will be registered when this application
    | is started. However, feel free to register as many as you wish as
    | the aliases are "lazy" loaded so they don't hinder performance.
    |
    */

    'aliases' => array(

        'App'             => 'Illuminate\Support\Facades\App',
        'Artisan'         => 'Illuminate\Support\Facades\Artisan',
        'Auth'            => 'Illuminate\Support\Facades\Auth',
        'Blade'           => 'Illuminate\Support\Facades\Blade',
        'Cache'           => 'Illuminate\Support\Facades\Cache',
        'ClassLoader'     => 'Illuminate\Support\ClassLoader',
        'Config'          => 'Illuminate\Support\Facades\Config',
        'Controller'      => 'Illuminate\Routing\Controllers\Controller',
        'Cookie'          => 'Illuminate\Support\Facades\Cookie',
        'Crypt'           => 'Illuminate\Support\Facades\Crypt',
        'DB'              => 'Illuminate\Support\Facades\DB',
        'Eloquent'        => 'Illuminate\Database\Eloquent\Model',
        'Event'           => 'Illuminate\Support\Facades\Event',
        'File'            => 'Illuminate\Support\Facades\File',
        'Form'            => 'Illuminate\Support\Facades\Form',
        'Hash'            => 'Illuminate\Support\Facades\Hash',
        'HTML'            => 'Illuminate\Support\Facades\HTML',
        'Input'           => 'Illuminate\Support\Facades\Input',
        'Lang'            => 'Illuminate\Support\Facades\Lang',
        'Log'             => 'Illuminate\Support\Facades\Log',
        'Mail'            => 'Illuminate\Support\Facades\Mail',
        'Paginator'       => 'Illuminate\Support\Facades\Paginator',
        'Password'        => 'Illuminate\Support\Facades\Password',
        'Queue'           => 'Illuminate\Support\Facades\Queue',
        'Redirect'        => 'Illuminate\Support\Facades\Redirect',
        'Redis'           => 'Illuminate\Support\Facades\Redis',
        'Request'         => 'Illuminate\Support\Facades\Request',
        'Response'        => 'Illuminate\Support\Facades\Response',
        'Route'           => 'Illuminate\Support\Facades\Route',
        'Schema'          => 'Illuminate\Support\Facades\Schema',
        'Seeder'          => 'Illuminate\Database\Seeder',
        'Session'         => 'Illuminate\Support\Facades\Session',
        'Str'             => 'Illuminate\Support\Str',
        'URL'             => 'Illuminate\Support\Facades\URL',
        'Validator'       => 'Illuminate\Support\Facades\Validator',
        'View'            => 'Illuminate\Support\Facades\View',
        'Sentry'          => 'Cartalyst\Sentry\Facades\Laravel\Sentry',
        'JobProvider'     => 'GrahamCampbell\Queuing\Facades\JobProvider',
        'Queuing'         => 'GrahamCampbell\Queuing\Facades\Queuing',
        'Cron'            => 'GrahamCampbell\Queuing\Facades\Cron',
        'HTMLMin'         => 'GrahamCampbell\HTMLMin\Facades\HTMLMin',
        'Security'        => 'GrahamCampbell\Security\Facades\Security',
        'Binput'          => 'GrahamCampbell\Binput\Facades\Binput',
        'Passwd'          => 'GrahamCampbell\Passwd\Facades\Passwd',
        'Navigation'      => 'GrahamCampbell\Navigation\Facades\Navigation',
        'CoreAPI'         => 'GrahamCampbell\CoreAPI\Facades\CoreAPI',
        'CloudFlareAPI'   => 'GrahamCampbell\CloudFlareAPI\Facades\CloudFlareAPI',
        'CommentProvider' => 'GrahamCampbell\CMSCore\Facades\CommentProvider',
        'EventProvider'   => 'GrahamCampbell\CMSCore\Facades\EventProvider',
        'FileProvider'    => 'GrahamCampbell\CMSCore\Facades\FileProvider',
        'FolderProvider'  => 'GrahamCampbell\CMSCore\Facades\FolderProvider',
        'GroupProvider'   => 'GrahamCampbell\CMSCore\Facades\GroupProvider',
        'PageProvider'    => 'GrahamCampbell\CMSCore\Facades\PageProvider',
        'PostProvider'    => 'GrahamCampbell\CMSCore\Facades\PostProvider',
        'ReplyProvider'   => 'GrahamCampbell\CMSCore\Facades\ReplyProvider',
        'SectionProvider' => 'GrahamCampbell\CMSCore\Facades\SectionProvider',
        'TopicProvider'   => 'GrahamCampbell\CMSCore\Facades\TopicProvider',
        'UserProvider'    => 'GrahamCampbell\CMSCore\Facades\UserProvider',
        'Markdown'        => 'VTalbot\Markdown\Facades\Markdown',
        'Asset'           => 'Lightgear\Asset\Facades\Asset',
        'Carbon'          => 'Carbon\Carbon',
        'Debugbar'        => 'Barryvdh\Debugbar\Facade',
        'PDF'             => 'Barryvdh\DomPDF\Facade'

    )

);