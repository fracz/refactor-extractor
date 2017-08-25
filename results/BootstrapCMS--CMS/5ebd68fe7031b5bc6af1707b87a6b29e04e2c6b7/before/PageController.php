<?php

use PageRepositoryInterface as Page;

class PageController extends BaseController {

    protected $page;

    /**
     * Load the injected model.
     * Setup access permissions.
     */
    public function __construct(Page $page) {
        $this->page = $page;
        $this->edits[] = 'create';
        $this->edits[] = 'store';
        $this->edits[] = 'edit';
        $this->edits[] = 'update';
        $this->edits[] = 'destroy';
        parent::__construct();
    }

    /**
     * Display a listing of the resource.
     *
     * @return Response
     */
    public function index() {
        Session::flash('', ''); // work around laravel bug
        Session::reflash();
        Log::info('Redirecting from pages to the home page');
        return Redirect::route('pages.show', array('pages' => 'home'));
    }

    /**
     * Show the form for creating a new resource.
     *
     * @return Response
     */
    public function create() {
        return View::make('pages.create');
    }

    /**
     * Store a newly created resource in storage.
     *
     * @return Response
     */
    public function store() {
        $page = new $this->page;

        $input = array(
            'title' => Binput::get('title'),
            'slug' => urlencode(strtolower(str_replace(' ', '-', Binput::get('title')))),
            'body' => Input::get('body'), // use standard input method
            'show_title' => (Binput::get('show_title') == 'on'),
            'show_nav' => (Binput::get('show_nav') == 'on'),
            'icon' => Binput::get('icon'),
            'user_id' => Sentry::getUser()->getId());

        $page->fill($input);

        if ($page->save()) {
            Session::flash('success', 'Your page has been created successfully.');
            return Redirect::route('pages.show', array('pages' => $page->slug));
        } else {
            return Redirect::route('pages.create')->withInput()->withErrors($page->errors());
        }
    }

    /**
     * Display the specified resource.
     *
     * @param  string  $slug
     * @return Response
     */
    public function show($slug) {
        $page = $this->page->findBySlug($slug);

        if (!$page) {
            if ($slug == 'home') {
                App::abort(500, 'The Homepage Is Missing');
            } else {
                App::abort(404, 'Page Not Found');
            }
        }

        return View::make('pages.show', array('page' => $page));
    }

    /**
     * Show the form for editing the specified resource.
     *
     * @param  string  $slug
     * @return Response
     */
    public function edit($slug) {
        $page = $this->page->findBySlug($slug);

        if (!$page) {
            if ($slug == 'home') {
                App::abort(500, 'The Homepage Is Missing');
            } else {
                App::abort(404, 'Page Not Found');
            }
        }

        return View::make('pages.edit', array('page' => $page));
    }

    /**
     * Update the specified resource in storage.
     *
     * @param  string  $slug
     * @return Response
     */
    public function update($slug) {
        $page = $this->page->findBySlug($slug);

        if (!$page) {
            if ($slug == 'home') {
                App::abort(500, 'The Homepage Is Missing');
            } else {
                App::abort(404, 'Page Not Found');
            }
        }

        $input = array(
            'title' => Binput::get('title'),
            'slug' => urlencode(strtolower(str_replace(' ', '-', Binput::get('title')))),
            'body' => Input::get('body'), // use standard input method
            'show_title' => (Binput::get('show_title') == 'on'),
            'show_nav' => (Binput::get('show_nav') == 'on'),
            'icon' => Binput::get('icon'),
            'user_id' => Sentry::getUser()->getId());

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

        $page->fill($input);

        if ($page->save()) {
            Session::flash('success', 'Your page has been updated successfully.');
            return Redirect::route('pages.show', array('pages' => $page->slug));
        } else {
            return Redirect::route('pages.edit', array('pages' => $slug))->withInput()->withErrors($page->errors());
        }
    }

    /**
     * Remove the specified resource from storage.
     *
     * @param  string  $slug
     * @return Response
     */
    public function destroy($slug) {
        $page = $this->page->findBySlug($slug);

        if (!$page) {
            if ($slug == 'home') {
                App::abort(500, 'The Homepage Is Missing');
            } else {
                App::abort(404, 'Page Not Found');
            }
        }

        if ($slug == 'home') {
            Session::flash('error', 'You cannot delete the homepage.');
            return Redirect::route('base');
        }

        $page->delete();

        Session::flash('success', 'Your page has been deleted successfully.');
        return Redirect::route('base');
    }
}