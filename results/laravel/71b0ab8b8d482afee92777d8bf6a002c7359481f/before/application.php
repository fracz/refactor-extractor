<?php

return array(

	/*
	|--------------------------------------------------------------------------
	| Application URL
	|--------------------------------------------------------------------------
	|
	| The URL used to access your application. No trailing slash.
	|
	*/

	'url' => 'http://localhost',

	/*
	|--------------------------------------------------------------------------
	| Application Index
	|--------------------------------------------------------------------------
	|
	| If you are including the "index.php" in your URLs, you can ignore this.
	|
	| However, if you are using mod_rewrite or something similar to get
	| cleaner URLs, set this option to an empty string.
	|
	*/

	'index' => 'index.php',

	/*
	|--------------------------------------------------------------------------
	| Application Language
	|--------------------------------------------------------------------------
	|
	| The default language of your application. This language will be used by
	| Lang library as the default language when doing string localization.
	|
	*/

	'language' => 'en',

	/*
	|--------------------------------------------------------------------------
	| Application Timezone
	|--------------------------------------------------------------------------
	|
	| The default timezone of your application. This timezone will be used when
	| Laravel needs a date, such as when writing to a log file.
	|
	*/

	'timezone' => 'UTC',

	/*
	|--------------------------------------------------------------------------
	| Application Character Encoding
	|--------------------------------------------------------------------------
	|
	| The default character encoding used by your application. This is the
	| character encoding that will be used by the Str, Text, and Form classes.
	|
	*/

	'encoding' => 'UTF-8',

	/*
	|--------------------------------------------------------------------------
	| Application Key
	|--------------------------------------------------------------------------
	|
	| Your application key should be a 32 character string that is totally
	| random and secret. This key is used by the encryption class to generate
	| secure, encrypted strings.
	|
	*/

	'key' => '',

	/*
	|--------------------------------------------------------------------------
	| SSL Link Generation
	|--------------------------------------------------------------------------
	|
	| Many sites use SSL to protect their users data. However, you may not
	| always be able to use SSL on your development machine, meaning all HTTPS
	| will be broken during development.
	|
	| For this reason, you may wish to disable the generation of HTTPS links
	| throughout your application. This option does just that. All attempts to
	| generate HTTPS links will generate regular HTTP links instead.
	|
	*/

	'ssl' => true,

	/*
	|--------------------------------------------------------------------------
	| Auto-Loaded Packages
	|--------------------------------------------------------------------------
	|
	| The packages that should be auto-loaded each time Laravel handles a
	| request. These should generally be packages that you use on almost every
	| request to your application.
	|
	| Each package specified here will be bootstrapped and can be conveniently
	| used by your application's routes, models, and libraries.
	|
	| Note: The package names in this array should correspond to a package
	|       directory in application/packages.
	|
	*/

	'packages' => array(),

);