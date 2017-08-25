<?php

/*
|--------------------------------------------------------------------------
| Application Routes
|--------------------------------------------------------------------------
|
| Here is where you can register all of the routes for an application.
| It's a breeze. Simply tell Laravel the URIs it should respond to
| and give it the Closure to execute when that URI is requested.
|
*/


// test routes
Route::get('hello', array('as' => 'hello', 'uses' => 'GrahamCampbell\BootstrapCMS\Controllers\HomeController@showWelcome'));
Route::get('test', array('as' => 'test', 'uses' => 'GrahamCampbell\BootstrapCMS\Controllers\HomeController@showTest'));
Route::get('testqueue', array('as' => 'testqueue', 'uses' => 'GrahamCampbell\BootstrapCMS\Controllers\HomeController@testQueue'));
Route::get('testerror', array('as' => 'testerror', 'uses' => 'GrahamCampbell\BootstrapCMS\Controllers\HomeController@testError'));
Route::get('add/{value}', array('as' => 'add', 'uses' => 'GrahamCampbell\BootstrapCMS\Controllers\HomeController@addValue'));
Route::get('get', array('as' => 'get', 'uses' => 'GrahamCampbell\BootstrapCMS\Controllers\HomeController@getValue'));


// send users to the home page
Route::get('/', array('as' => 'base', function() {
    Session::flash('', ''); // work around laravel bug
    Session::reflash();
    Log::info('Redirecting from / to the home page');
    return Redirect::route('pages.show', array('pages' => 'home'));
}));

// send users to the posts page
if (Config::get('cms.blogging')) {
    Route::get('blog', array('as' => 'blog', function() {
        Session::flash('', ''); // work around laravel bug
        Session::reflash();
        Log::info('Redirecting from blog to the posts page');
        return Redirect::route('blog.posts.index');
    }));
}

// send users to the profile page
Route::get('account', array('as' => 'account', function() {
    Session::flash('', ''); // work around laravel bug
    Session::reflash();
    Log::info('Redirecting from account to the profile page');
    return Redirect::route('account.profile');
}));


// account routes
Route::get('account/profile', array('as' => 'account.profile', 'uses' => 'GrahamCampbell\BootstrapCMS\Controllers\AccountController@getProfile'));
Route::delete('account/profile', array('as' => 'account.profile.delete', 'uses' => 'GrahamCampbell\BootstrapCMS\Controllers\AccountController@deleteProfile'));
Route::patch('account/details', array('as' => 'account.details.patch', 'uses' => 'GrahamCampbell\BootstrapCMS\Controllers\AccountController@patchDetails'));
Route::patch('account/password', array('as' => 'account.password.patch', 'uses' => 'GrahamCampbell\BootstrapCMS\Controllers\AccountController@patchPassword'));


// login routes
Route::get('account/login', array('as' => 'account.login', 'uses' => 'GrahamCampbell\BootstrapCMS\Controllers\LoginController@getLogin'));
Route::post('account/login', array('as' => 'account.login.post', 'uses' => 'GrahamCampbell\BootstrapCMS\Controllers\LoginController@postLogin'));
Route::get('account/logout', array('as' => 'account.logout', 'uses' => 'GrahamCampbell\BootstrapCMS\Controllers\LoginController@getLogout'));


// reset route
Route::get('account/reset', array('as' => 'account.reset', 'uses' => 'GrahamCampbell\BootstrapCMS\Controllers\ResetController@getReset'));
Route::post('account/reset', array('as' => 'account.reset.post', 'uses' => 'GrahamCampbell\BootstrapCMS\Controllers\ResetController@postReset'));
Route::get('account/password/{id}/{code}', array('as' => 'account.password', 'uses' => 'GrahamCampbell\BootstrapCMS\Controllers\ResetController@getPassword'));


// registration routes
Route::get('account/register', array('as' => 'account.register', 'uses' => 'GrahamCampbell\BootstrapCMS\Controllers\RegistrationController@getRegister'));
Route::post('account/register', array('as' => 'account.register.post', 'uses' => 'GrahamCampbell\BootstrapCMS\Controllers\RegistrationController@postRegister'));
Route::get('account/activate/{id}/{code}', array('as' => 'account.activate', 'uses' => 'GrahamCampbell\BootstrapCMS\Controllers\RegistrationController@getActivate'));



// user routes
Route::resource('users', 'GrahamCampbell\BootstrapCMS\Controllers\UserController');

// page routes
Route::resource('pages', 'GrahamCampbell\BootstrapCMS\Controllers\PageController');

// event routes
if (Config::get('cms.events')) {
    Route::resource('events', 'GrahamCampbell\BootstrapCMS\Controllers\EventController');
}

// blog routes
if (Config::get('cms.blogging')) {
    Route::resource('blog/posts', 'GrahamCampbell\BootstrapCMS\Controllers\PostController');
    Route::resource('blog/posts.comments', 'GrahamCampbell\BootstrapCMS\Controllers\CommentController');
}