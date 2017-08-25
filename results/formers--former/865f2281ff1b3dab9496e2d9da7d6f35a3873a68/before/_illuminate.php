<?php
/**
 * IlluminateMock
 *
 * Dummy Illuminate app for testing purposes
 */
class IlluminateMock
{
  public $app;

  public function __construct()
  {
    $app = new Illuminate\Container;

    // Setup Illuminate
    $app['config']     = $this->getConfig();
    $app['request']    = $this->getRequest();
    $app['session']    = $this->getSession();
    $app['translator'] = $this->getTranslator();
    $app['url']        = $this->getUrl();
    $app['validator']  = $this->getValidator();

    // Setup bindings
    $app['former.laravel.form'] = $app->share(function($app) { return new Laravel\Form($app); });
    $app['former.laravel.file'] = $app->share(function($app) { return new Laravel\File($app); });
    $app['former'] = $app->share(function($app) { return new Former\Former($app); });
    $app['former.helpers'] = $app->share(function($app) { return new Former\Helpers($app); });
    $app['former.framework'] = $app->share(function($app) { return new Former\Framework\TwitterBootstrap($app); });

    $this->app = $app;

    return $this;
  }

  /**
   * Get the container
   */
  public function get()
  {
    return $this->app;
  }

  /**
   * Get config manager
   *
   * @param boolean $live      Whether live validation should be active
   * @param string  $unchecked The checkable unchecked value
   * @param boolean $push      Whether unchecked checkboxes should be pushed
   * @param boolean $automatic Automatic live validation or not
   */
  public function getConfig($live = true, $unchecked = '', $push = false, $automatic = true)
  {
    $config = Mockery::mock('config');
    $config->shouldReceive('get')->with('application.encoding')->andReturn('UTF-8');
    $config->shouldReceive('get')->with('former::default_form_type')->andReturn('horizontal');
    $config->shouldReceive('get')->with('former::fetch_errors')->andReturn(false);
    $config->shouldReceive('get')->with('former::framework')->andReturn('bootstrap');
    $config->shouldReceive('get')->with('former::translate_from')->andReturn('validation.attributes');
    $config->shouldReceive('get')->with('former::required_class')->andReturn('required');
    $config->shouldReceive('get')->with('former::required_text')->andReturn('<sup>*</sup>');

    // Variable configuration keys
    $config->shouldReceive('get')->with('former::live_validation')->andReturn($live);
    $config->shouldReceive('get')->with('former::unchecked_value')->andReturn($unchecked);
    $config->shouldReceive('get')->with('former::push_checkboxes')->andReturn($push);
    $config->shouldReceive('get')->with('former::automatic_label')->andReturn($automatic);

    return $config;
  }

  /**
   * Get URL manager
   */
  private function getUrl()
  {
    $url = Mockery::mock('url');
    $url->shouldReceive('to')->andReturnUsing(function($url) {
      return $url == '#' ? $url : 'https://test/en/'.$url;
    });

    return $url;
  }

  /**
   * Get Validator
   */
  private function getValidator()
  {
    $validator = Mockery::mock('Illuminate\Validation\Validator');
    $validator->shouldReceive('getMessages')->andReturnUsing(function() {
      $messages = Mockery::mock('MessageBag');
      $messages->shouldReceive('first')->with('required')->andReturn('The required field is required.');
      return $messages;
    });

    return $validator;
  }

  /**
   * Get Session manager
   */
  private function getSession()
  {
    $session = Mockery::mock('session');
    $session->shouldReceive('has')->with('errors')->andReturn(false);
    $session->shouldReceive('set')->with('errors')->andReturn(false);
    $session->shouldReceive('getToken')->andReturn('csrf_token');

    return $session;
  }

  /**
   * Get localization manager
   */
  private function getTranslator()
  {
    $translator = Mockery::mock('Illuminate\Translation\Translator');
    $translator->shouldReceive('get')->with('pagination.next')->andReturn('Next &raquo;');
    $translator->shouldReceive('get')->with('pagination')->andReturn(array('previous' => '&laquo; Previous', 'next' => 'Next &raquo;'));
    $translator->shouldReceive('get')->withAnyArgs()->andReturnUsing(function($key) {
      return $key;
    });
    $translator->shouldReceive('has')->withAnyArgs()->andReturn(true);

    return $translator;
  }

  /**
   * Get request manager
   */
  private function getRequest()
  {
    $request = Mockery::mock('request');
    $request->shouldReceive('url')->andReturn('#');
    $request->shouldReceive('get')->andReturn(null)->byDefault();
    $request->shouldReceive('old')->andReturn(null);

    return $request;
  }
}