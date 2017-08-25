<?php namespace GrahamCampbell\BootstrapCMS\Controllers;

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

use App;
use Redirect;
use Session;
use Validator;

use Binput;

use PageProvider;
use GrahamCampbell\CMSCore\Models\Page;

use GrahamCampbell\CMSCore\Controllers\BaseController;

class PageController extends BaseController {

    /**
     * Constructor (setup access permissions).
     *
     * @return void
     */
    public function __construct() {
        $this->setPermissions(array(
            'create'  => 'edit',
            'store'   => 'edit',
            'edit'    => 'edit',
            'update'  => 'edit',
            'destroy' => 'edit',
        ));

        parent::__construct();
    }

    /**
     * Redirect to the homepage.
     *
     * @return \Illuminate\Http\Response
     */
    public function index() {
        Session::flash('', ''); // work around laravel bug if there is no session yet
        Session::reflash();
        return Redirect::route('pages.show', array('pages' => 'home'));
    }

    /**
     * Show the form for creating a new page.
     *
     * @return \Illuminate\Http\Response
     */
    public function create() {
        return $this->viewMake('pages.create');
    }

    /**
     * Store a new page.
     *
     * @return \Illuminate\Http\Response
     */
    public function store() {
        $input = array(
            'title'      => Binput::get('title'),
            'slug'       => urlencode(strtolower(str_replace(' ', '-', Binput::get('title')))),
            'body'       => Binput::get('body', null, true, false), // no xss protection please
            'show_title' => (Binput::get('show_title') == 'on'),
            'show_nav'   => (Binput::get('show_nav') == 'on'),
            'icon'       => Binput::get('icon'),
            'user_id'    => $this->getUserId(),
        );

        $rules = Page::$rules;

        $val = Validator::make($input, $rules);
        if ($val->fails()) {
            return Redirect::route('pages.create')->withInput()->withErrors($val->errors());
        }

        $page = PageProvider::create($input);

        // write flash message and redirect
        Session::flash('success', 'Your page has been created successfully.');
        return Redirect::route('pages.show', array('pages' => $page->getSlug()));
    }

    /**
     * Show the specified page.
     *
     * @param  string  $slug
     * @return \Illuminate\Http\Response
     */
    public function show($slug) {
        $page = PageProvider::find($slug);
        $this->checkPage($page, $slug);

        return $this->viewMake('pages.show', array('page' => $page));
    }

    /**
     * Show the form for editing the specified page.
     *
     * @param  string  $slug
     * @return \Illuminate\Http\Response
     */
    public function edit($slug) {
        $page = PageProvider::find($slug);
        $this->checkPage($page, $slug);

        return $this->viewMake('pages.edit', array('page' => $page));
    }

    /**
     * Update an existing page.
     *
     * @param  string  $slug
     * @return \Illuminate\Http\Response
     */
    public function update($slug) {
        $input = array(
            'title'      => Binput::get('title'),
            'slug'       => urlencode(strtolower(str_replace(' ', '-', Binput::get('title')))),
            'body'       => Binput::get('body', null, true, false), // no xss protection please
            'css'        => Binput::get('css', null, true, false), // no xss protection please
            'js'         => Binput::get('js', null, true, false), // no xss protection please
            'show_title' => (Binput::get('show_title') == 'on'),
            'show_nav'   => (Binput::get('show_nav') == 'on'),
            'icon'       => Binput::get('icon'),
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

        $page = PageProvider::find($slug);
        $this->checkPage($page, $slug);

        $checkupdate = $this->checkUpdate($input, $slug);
        if ($checkupdate) {
            return $checkupdate;
        }

        $page->update($input);

        // write flash message and redirect
        Session::flash('success', 'Your page has been updated successfully.');
        return Redirect::route('pages.show', array('pages' => $page->getSlug()));
    }

    /**
     * Delete an existing page.
     *
     * @param  string  $slug
     * @return \Illuminate\Http\Response
     */
    public function destroy($slug) {
        $page = PageProvider::find($slug);
        $this->checkPage($page, $slug);

        $checkdelete = $this->checkDelete($slug);
        if ($checkdelete) {
            return $checkdelete;
        }

        $page->delete();

        // write flash message and redirect
        Session::flash('success', 'Your page has been deleted successfully.');
        return Redirect::route('pages.show', array('pages' => 'home'));
    }

    /**
     * Check the page model.
     *
     * @return mixed
     */
    protected function checkPage($page, $slug) {
        if (!$page) {
            if ($slug == 'home') {
                return App::abort(500, 'The Homepage Is Missing');
            }

            return App::abort(404, 'Page Not Found');
        }
    }

    /**
     * Check the update input.
     *
     * @return mixed
     */
    protected function checkUpdate($input, $slug) {
        if ($slug == 'home') {
            if ($slug != $input['slug']) {
                Session::flash('error', 'You cannot rename the homepage.');
                return Redirect::route('pages.edit', array('pages' => $slug))->withInput();
            }

            if ($input['show_nav'] == false) {
                Session::flash('error', 'The homepage must be on the navigation bar.');
                return Redirect::route('pages.edit', array('pages' => $slug))->withInput();
            }
        }
    }

    /**
     * Check the delete input.
     *
     * @return mixed
     */
    protected function checkDelete($slug) {
        if ($slug == 'home') {
            Session::flash('error', 'You cannot delete the homepage.');
            return Redirect::route('pages.show', array('pages' => 'home'));
        }
    }
}