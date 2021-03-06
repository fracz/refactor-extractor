<?php namespace Laravel;

return array(

	/*
	|--------------------------------------------------------------------------
	| Laravel Support Components
	|--------------------------------------------------------------------------
	*/

	'laravel.database' => array('singleton' => true, 'resolver' => function($container)
	{
		$config = $container->resolve('laravel.config');

		$connections = $config->get('database.connections');

		return new Database\Manager($config->get('database.connections'), $config->get('database.default'));
	}),


	'laravel.file' => array('singleton' => true, 'resolver' => function($container)
	{
		return new File($container->resolve('laravel.config')->get('mimes'));
	}),


	'laravel.form' => array('singleton' => true, 'resolver' => function($container)
	{
		$request = $container->resolve('laravel.request');

		$html = $container->resolve('laravel.html');

		$url = $container->resolve('laravel.url');

		$token = ($container->registered('laravel.session.driver')) ? $container->resolve('laravel.session.driver')->get('csrf_token') : null;

		return new Form($request, $html, $url, $token);
	}),


	'laravel.html' => array('singleton' => true, 'resolver' => function($container)
	{
		$encoding = $container->resolve('laravel.config')->get('application.encoding');

		return new HTML($container->resolve('laravel.url'), $encoding);
	}),


	'laravel.input' => array('singleton' => true, 'resolver' => function($container)
	{
		$application = $container->resolve('laravel.application');

		$input = array();

		if ($application->request->method == 'GET')
		{
			$input = $_GET;
		}
		elseif ($application->request->method == 'POST')
		{
			$input = $_POST;
		}
		elseif ($application->request->method == 'PUT' or $application->request->method == 'DELETE')
		{
			($application->request->spoofed) ? $input = $_POST : parse_str(file_get_contents('php://input'), $input);
		}

		return new Input($input, $_FILES, new Cookie($_COOKIE));
	}),


	'laravel.package' => array('singleton' => true, 'resolver' => function()
	{
		return new Package;
	}),


	'laravel.responder' => array('singleton' => true, 'resolver' => function($container)
	{
		require_once SYS_PATH.'response'.PHP;

		return new Response_Factory($container->resolve('laravel.view'), $container->resolve('laravel.file'));
	}),


	'laravel.router' => array('singleton' => true, 'resolver' => function($container)
	{
		return new Routing\Router($container->resolve('laravel.request'), require APP_PATH.'routes'.EXT, CONTROLLER_PATH);
	}),


	'laravel.url' => array('singleton' => true, 'resolver' => function($container)
	{
		$config = $container->resolve('laravel.config');

		$request = $container->resolve('laravel.request');

		$base = $config->get('application.url');

		$index = $config->get('application.index');

		return new URL($container->resolve('laravel.router'), $base, $index, $request->secure);
	}),


	'laravel.view' => array('singleton' => true, 'resolver' => function()
	{
		require_once SYS_PATH.'view'.EXT;

		return new View_Factory(require APP_PATH.'composers'.EXT, VIEW_PATH);
	}),

	/*
	|--------------------------------------------------------------------------
	| Laravel Security Components
	|--------------------------------------------------------------------------
	*/

	'laravel.security.auth' => array('resolver' => function($container)
	{
		$hasher = $container->resolve('laravel.security.hashing.engine');

		return new Security\Auth(Session\Manager::driver(), $hasher);
	}),


	'laravel.security.hashing.engine' => array('resolver' => function()
	{
		return new Security\Hashing\BCrypt(10, false);
	}),

	/*
	|--------------------------------------------------------------------------
	| Laravel Cookie Session Components
	|--------------------------------------------------------------------------
	*/

	'laravel.session.cookie' => array('resolver' => function($container)
	{
		$cookies = $container->resolve('laravel.request')->input->cookies;

		$config = $container->resolve('laravel.config')->get('session');

		return new Session\Cookie(Security\Crypter::make(), $cookies, $config);
	}),

	/*
	|--------------------------------------------------------------------------
	| Laravel Database Session Components
	|--------------------------------------------------------------------------
	*/

	'laravel.session.database' => array('resolver' => function($container)
	{
		$table = $container->resolve('laravel.config')->get('session.table');

		return new Session\Database($container->resolve('laravel.database.manager')->connection(), $table);
	}),

	/*
	|--------------------------------------------------------------------------
	| Laravel Cache Manager
	|--------------------------------------------------------------------------
	*/

	'laravel.cache' => array('singleton' => true, 'resolver' => function($container)
	{
		return new Cache\Manager($container, $container->resolve('laravel.config')->get('cache.driver'));
	}),

	/*
	|--------------------------------------------------------------------------
	| Laravel File Cache & Session Components
	|--------------------------------------------------------------------------
	*/

	'laravel.cache.file' => array('resolver' => function($container)
	{
		return new Cache\File($container->resolve('laravel.file'), CACHE_PATH);
	}),


	'laravel.session.file' => array('resolver' => function($container)
	{
		return new Session\File($container->resolve('laravel.file'), SESSION_PATH);
	}),

	/*
	|--------------------------------------------------------------------------
	| Laravel APC Cache & Session Components
	|--------------------------------------------------------------------------
	*/

	'laravel.cache.apc' => array('resolver' => function($container)
	{
		return new Cache\APC(new Cache\APC_Engine, $container->resolve('laravel.config')->get('cache.key'));
	}),


	'laravel.session.apc' => array('resolver' => function($container)
	{
		$lifetime = $container->resolve('laravel.config')->get('session.lifetime');

		return new Session\APC($container->resolve('laravel.cache.apc'), $lifetime);
	}),

	/*
	|--------------------------------------------------------------------------
	| Laravel Memcached Cache & Session Components
	|--------------------------------------------------------------------------
	*/

	'laravel.cache.memcached' => array('resolver' => function($container)
	{
		$connection = $container->resolve('laravel.cache.memcache.connection');

		$key = $container->resolve('laravel.config')->get('cache.key');

		return new Cache\Memcached($connection, $key);
	}),


	'laravel.session.memcached' => array('resolver' => function($container)
	{
		$lifetime = $container->resolve('laravel.config')->get('session.lifetime');

		return new Session\Memcached($container->resolve('laravel.cache.memcached'), $lifetime);
	}),


	'laravel.cache.memcache.connection' => array('singleton' => true, 'resolver' => function()
	{
		if ( ! class_exists('Memcache'))
		{
			throw new \Exception('Attempting to use Memcached, but the Memcache PHP extension is not installed on this server.');
		}

		$memcache = new \Memcache;

		foreach (Config::get('cache.servers') as $server)
		{
			$memcache->addServer($server['host'], $server['port'], true, $server['weight']);
		}

		if ($memcache->getVersion() === false)
		{
			throw new \Exception('Memcached is configured. However, no connections could be made. Please verify your memcached configuration.');
		}

		return $memcache;
	}),

);