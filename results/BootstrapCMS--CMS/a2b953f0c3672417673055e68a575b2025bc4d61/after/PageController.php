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
 */

namespace GrahamCampbell\BootstrapCMS\Controllers;

use Illuminate\Session\SessionManager;
use Illuminate\Support\Facades\Redirect;
use Illuminate\Support\Facades\Validator;
use GrahamCampbell\Binput\Classes\Binput;
use GrahamCampbell\Viewer\Classes\Viewer;
use GrahamCampbell\BootstrapCMS\Models\Page;
use GrahamCampbell\BootstrapCMS\Classes\PageProvider;
use GrahamCampbell\Credentials\Classes\Credentials;
use Symfony\Component\HttpKernel\Exception\NotFoundHttpException;

/**
 * This is the page controller class.
 *
 * @package    Bootstrap-CMS
 * @author     Graham Campbell
 * @copyright  Copyright (C) 2013-2014  Graham Campbell
 * @license    https://github.com/GrahamCampbell/Bootstrap-CMS/blob/master/LICENSE.md
 * @link       https://github.com/GrahamCampbell/Bootstrap-CMS
 */
class PageController extends AbstractController
{
    /**
     * The viewer instance.
     *
     * @var \GrahamCampbell\Viewer\Classes\Viewer
     */
    protected $viewer;

    /**
     * The session instance.
     *
     * @var \Illuminate\Session\SessionManager
     */
    protected $session;

    /**
     * The binput instance.
     *
     * @var \GrahamCampbell\Binput\Classes\Binput
     */
    protected $binput;

    /**
     * The page provider instance.
     *
     * @var \GrahamCampbell\BootstrapCMS\Providers\PageProvider
     */
    protected $pageprovider;

    /**
     * Create a new instance.
     *
     * @param  \GrahamCampbell\Credentials\Classes\Credentials  $credentials
     * @param  \GrahamCampbell\Viewer\Classes\Viewer  $viewer
     * @param  \Illuminate\Session\SessionManager  $session
     * @param  \GrahamCampbell\Binput\Classes\Binput  $binput
     * @param  \GrahamCampbell\BootstrapCMS\Providers\PageProvider  $pageprovider
     * @return void
     */
    public function __construct(Credentials $credentials, Viewer $viewer, SessionManager $session, Binput $binput, PageProvider $pageprovider)
    {
        $this->viewer = $viewer;
        $this->session = $session;
        $this->binput = $binput;
        $this->pageprovider = $pageprovider;

        $this->setPermissions(array(
            'create'  => 'edit',
            'store'   => 'edit',
            'edit'    => 'edit',
            'update'  => 'edit',
            'destroy' => 'edit',
        ));

        parent::__construct($credentials);
    }

    /**
     * Redirect to the homepage.
     *
     * @return \Illuminate\Http\Response
     */
    public function index()
    {
        $this->session->flash('', ''); // work around laravel bug if there is no session yet
        $this->session->reflash();
        return Redirect::route('pages.show', array('pages' => 'home'));
    }

    /**
     * Show the form for creating a new page.
     *
     * @return \Illuminate\Http\Response
     */
    public function create()
    {
        return $this->viewer->make('pages.create');
    }

    /**
     * Store a new page.
     *
     * @return \Illuminate\Http\Response
     */
    public function store()
    {
        $input = array(
            'title'      => $this->binput->get('title'),
            'slug'       => urlencode(strtolower(str_replace(' ', '-', $this->binput->get('title')))),
            'body'       => $this->binput->get('body', null, true, false), // no xss protection please
            'show_title' => ($this->binput->get('show_title') == 'on'),
            'show_nav'   => ($this->binput->get('show_nav') == 'on'),
            'icon'       => $this->binput->get('icon'),
            'user_id'    => $this->getUserId(),
        );

        $rules = Page::$rules;

        $val = Validator::make($input, $rules);
        if ($val->fails()) {
            return Redirect::route('pages.create')->withInput()->withErrors($val->errors());
        }

        $page = $this->pageprovider->create($input);

        // write flash message and redirect
        $this->session->flash('success', 'Your page has been created successfully.');
        return Redirect::route('pages.show', array('pages' => $page->slug));
    }

    /**
     * Show the specified page.
     *
     * @param  string  $slug
     * @return \Illuminate\Http\Response
     */
    public function show($slug)
    {
        $page = $this->pageprovider->find($slug);
        $this->checkPage($page, $slug);

        return $this->viewer->make('pages.show', array('page' => $page));
    }

    /**
     * Show the form for editing the specified page.
     *
     * @param  string  $slug
     * @return \Illuminate\Http\Response
     */
    public function edit($slug)
    {
        $page = $this->pageprovider->find($slug);
        $this->checkPage($page, $slug);

        return $this->viewer->make('pages.edit', array('page' => $page));
    }

    /**
     * Update an existing page.
     *
     * @param  string  $slug
     * @return \Illuminate\Http\Response
     */
    public function update($slug)
    {
        $input = array(
            'title'      => $this->binput->get('title'),
            'slug'       => urlencode(strtolower(str_replace(' ', '-', $this->binput->get('title')))),
            'body'       => $this->binput->get('body', null, true, false), // no xss protection please
            'css'        => $this->binput->get('css', null, true, false), // no xss protection please
            'js'         => $this->binput->get('js', null, true, false), // no xss protection please
            'show_title' => ($this->binput->get('show_title') == 'on'),
            'show_nav'   => ($this->binput->get('show_nav') == 'on'),
            'icon'       => $this->binput->get('icon'),
        );

        if (is_null($input['css']) || empty($input['css'])) {
            $input['css'] = '';
        }

        if (is_null($input['js']) || empty($input['js'])) {
            $input['js'] = '';
        }

        $rules = Page::$rules;
        unset($rules['user_id']);

        $val = Validator::make($input, $rules);
        if ($val->fails()) {
            return Redirect::route('pages.edit', array('pages' => $slug))->withInput()->withErrors($val->errors());
        }

        $page = $this->pageprovider->find($slug);
        $this->checkPage($page, $slug);

        $checkupdate = $this->checkUpdate($input, $slug);
        if ($checkupdate) {
            return $checkupdate;
        }

        $page->update($input);

        // write flash message and redirect
        $this->session->flash('success', 'Your page has been updated successfully.');
        return Redirect::route('pages.show', array('pages' => $page->slug));
    }

    /**
     * Delete an existing page.
     *
     * @param  string  $slug
     * @return \Illuminate\Http\Response
     */
    public function destroy($slug)
    {
        $page = $this->pageprovider->find($slug);
        $this->checkPage($page, $slug);

        $checkdelete = $this->checkDelete($slug);
        if ($checkdelete) {
            return $checkdelete;
        }

        $page->delete();

        // write flash message and redirect
        $this->session->flash('success', 'Your page has been deleted successfully.');
        return Redirect::route('pages.show', array('pages' => 'home'));
    }

    /**
     * Check the page model.
     *
     * @param  mixed   $page
     * @param  string  $slug
     * @return \Illuminate\Http\Response
     */
    protected function checkPage($page, $slug)
    {
        if (!$page) {
            if ($slug == 'home') {
                throw new \Exception('The Homepage Is Missing');
            }

            throw new NotFoundHttpException('Page Not Found');
        }
    }

    /**
     * Check the update input.
     *
     * @param  array   $input
     * @param  string  $slug
     * @return \Illuminate\Http\Response
     */
    protected function checkUpdate(array $input, $slug)
    {
        if ($slug == 'home') {
            if ($slug != $input['slug']) {
                $this->session->flash('error', 'You cannot rename the homepage.');
                return Redirect::route('pages.edit', array('pages' => $slug))->withInput();
            }

            if ($input['show_nav'] == false) {
                $this->session->flash('error', 'The homepage must be on the navigation bar.');
                return Redirect::route('pages.edit', array('pages' => $slug))->withInput();
            }
        }
    }

    /**
     * Check the delete input.
     *
     * @param  string  $slug
     * @return \Illuminate\Http\Response
     */
    protected function checkDelete($slug)
    {
        if ($slug == 'home') {
            $this->session->flash('error', 'You cannot delete the homepage.');
            return Redirect::route('pages.show', array('pages' => 'home'));
        }
    }

    /**
     * Return the viewer instance.
     *
     * @return \GrahamCampbell\Viewer\Classes\Viewer
     */
    public function getViewer()
    {
        return $this->viewer;
    }

    /**
     * Return the session instance.
     *
     * @return \Illuminate\Session\SessionManager
     */
    public function getSession()
    {
        return $this->session;
    }

    /**
     * Return the binput instance.
     *
     * @return \GrahamCampbell\Binput\Classes\Binput
     */
    public function getBinput()
    {
        return $this->binput;
    }

    /**
     * Return the page provider instance.
     *
     * @return \GrahamCampbell\BootstrapCMS\Providers\PageProvider
     */
    public function getPageProvider()
    {
        return $this->pageprovider;
    }
}