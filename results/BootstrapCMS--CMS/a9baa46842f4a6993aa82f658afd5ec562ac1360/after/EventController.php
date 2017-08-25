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

use EventProvider;
use GrahamCampbell\CMSCore\Models\Event;

use GrahamCampbell\CMSCore\Controllers\BaseController;

class EventController extends BaseController {

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
     * Display a listing of the events.
     *
     * @return \Illuminate\Http\Response
     */
    public function index() {
        $events = EventProvider::paginate();
        $links = EventProvider::links();

        return $this->viewMake('events.index', array('events' => $events, 'links' => $links));
    }

    /**
     * Show the form for creating a new event.
     *
     * @return \Illuminate\Http\Response
     */
    public function create() {
        return $this->viewMake('events.create');
    }

    /**
     * Store a new event.
     *
     * @return \Illuminate\Http\Response
     */
    public function store() {
        $input = array(
            'title'    => Binput::get('title'),
            'location' => Binput::get('location'),
            'date'     => Binput::get('date'),
            'body'     => Binput::get('body'),
            'user_id'  => $this->getUserId(),
        );

        $rules = Event::$rules;

        $val = Validator::make($input, $rules);
        if ($val->fails()) {
            return Redirect::route('events.create')->withInput()->withErrors($val->errors());
        }

        $event = EventProvider::create($input);

        Session::flash('success', 'Your event has been created successfully.');
        return Redirect::route('events.show', array('events' => $event->getId()));
    }

    /**
     * Show the specified event.
     *
     * @param  int  $id
     * @return \Illuminate\Http\Response
     */
    public function show($id) {
        $event = EventProvider::find($id);
        $this->checkEvent($event);

        return $this->viewMake('events.show', array('event' => $event));
    }

    /**
     * Show the form for editing the specified event.
     *
     * @param  int  $id
     * @return \Illuminate\Http\Response
     */
    public function edit($id) {
        $event = EventProvider::find($id);
        $this->checkEvent($event);

        return $this->viewMake('events.edit', array('event' => $event));
    }

    /**
     * Update an existing event.
     *
     * @param  int  $id
     * @return \Illuminate\Http\Response
     */
    public function update($id) {
       $input = array(
            'title'    => Binput::get('title'),
            'location' => Binput::get('location'),
            'date'     => Binput::get('date'),
            'body'     => Binput::get('body'),
            'user_id'  => $this->getUserId(),
        );

        $rules = Event::$rules;
        unset($rules['user_id']);

        $val = Validator::make($input, $rules);
        if ($val->fails()) {
            return Redirect::route('events.edit', array('events' => $id))->withInput()->withErrors($val->errors());
        }

        $event = EventProvider::find($id);
        $this->checkEvent($event);

        $event->update($input);

        Session::flash('success', 'Your event has been updated successfully.');
        return Redirect::route('events.show', array('events' => $event->getId()));
    }

    /**
     * Delete an existing event.
     *
     * @param  int  $id
     * @return \Illuminate\Http\Response
     */
    public function destroy($id) {
        $event = EventProvider::find($id);
        $this->checkEvent($event);

        $event->delete();

        Session::flash('success', 'Your event has been deleted successfully.');
        return Redirect::route('events.index');
    }

    /**
     * Check the comment model.
     *
     * @return mixed
     */
    protected function checkEvent($event) {
        if (!$event) {
            return App::abort(404, 'Event Not Found');
        }
    }
}