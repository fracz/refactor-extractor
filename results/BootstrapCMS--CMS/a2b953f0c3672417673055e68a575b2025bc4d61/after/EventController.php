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

use Carbon\Carbon;
use Illuminate\Session\SessionManager;
use Illuminate\Support\Facades\Redirect;
use Illuminate\Support\Facades\Validator;
use GrahamCampbell\Binput\Classes\Binput;
use GrahamCampbell\Viewer\Classes\Viewer;
use GrahamCampbell\BootstrapCMS\Models\Event;
use GrahamCampbell\BootstrapCMS\Classes\EventProvider;
use GrahamCampbell\Credentials\Classes\Credentials;
use Symfony\Component\HttpKernel\Exception\NotFoundHttpException;

/**
 * This is the event controller class.
 *
 * @package    Bootstrap-CMS
 * @author     Graham Campbell
 * @copyright  Copyright (C) 2013-2014  Graham Campbell
 * @license    https://github.com/GrahamCampbell/Bootstrap-CMS/blob/master/LICENSE.md
 * @link       https://github.com/GrahamCampbell/Bootstrap-CMS
 */
class EventController extends AbstractController
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
     * The event provider instance.
     *
     * @var \GrahamCampbell\BootstrapCMS\Providers\EventProvider
     */
    protected $eventprovider;

    /**
     * Create a new instance.
     *
     * @param  \GrahamCampbell\Credentials\Classes\Credentials  $credentials
     * @param  \GrahamCampbell\Viewer\Classes\Viewer  $viewer
     * @param  \Illuminate\Session\SessionManager  $session
     * @param  \GrahamCampbell\Binput\Classes\Binput  $binput
     * @param  \GrahamCampbell\BootstrapCMS\Providers\EventProvider  $eventprovider
     * @return void
     */
    public function __construct(Credentials $credentials, Viewer $viewer, SessionManager $session, Binput $binput, EventProvider $eventprovider)
    {
        $this->viewer = $viewer;
        $this->session = $session;
        $this->binput = $binput;
        $this->eventprovider = $eventprovider;

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
     * Display a listing of the events.
     *
     * @return \Illuminate\Http\Response
     */
    public function index()
    {
        $events = $this->eventprovider->paginate();
        $links = $this->eventprovider->links();

        return $this->viewer->make('events.index', array('events' => $events, 'links' => $links));
    }

    /**
     * Show the form for creating a new event.
     *
     * @return \Illuminate\Http\Response
     */
    public function create()
    {
        return $this->viewer->make('events.create');
    }

    /**
     * Store a new event.
     *
     * @return \Illuminate\Http\Response
     */
    public function store()
    {
        $input = array(
            'title'    => $this->binput->get('title'),
            'location' => $this->binput->get('location'),
            'date'     => $this->binput->get('date'),
            'body'     => $this->binput->get('body'),
            'user_id'  => $this->getUserId(),
        );

        $rules = Event::$rules;

        $val = Validator::make($input, $rules);
        if ($val->fails()) {
            return Redirect::route('events.create')->withInput()->withErrors($val->errors());
        }

        $input['date'] = Carbon::createFromFormat('d/m/Y H:i', $input['date']);

        $event = $this->eventprovider->create($input);

        $this->session->flash('success', 'Your event has been created successfully.');
        return Redirect::route('events.show', array('events' => $event->id));
    }

    /**
     * Show the specified event.
     *
     * @param  int  $id
     * @return \Illuminate\Http\Response
     */
    public function show($id)
    {
        $event = $this->eventprovider->find($id);
        $this->checkEvent($event);

        return $this->viewer->make('events.show', array('event' => $event));
    }

    /**
     * Show the form for editing the specified event.
     *
     * @param  int  $id
     * @return \Illuminate\Http\Response
     */
    public function edit($id)
    {
        $event = $this->eventprovider->find($id);
        $this->checkEvent($event);

        return $this->viewer->make('events.edit', array('event' => $event));
    }

    /**
     * Update an existing event.
     *
     * @param  int  $id
     * @return \Illuminate\Http\Response
     */
    public function update($id)
    {
        $input = array(
            'title'    => $this->binput->get('title'),
            'location' => $this->binput->get('location'),
            'date'     => $this->binput->get('date'),
            'body'     => $this->binput->get('body'),
            'user_id'  => $this->getUserId(),
        );

        $rules = Event::$rules;
        unset($rules['user_id']);

        $val = Validator::make($input, $rules);
        if ($val->fails()) {
            return Redirect::route('events.edit', array('events' => $id))->withInput()->withErrors($val->errors());
        }

        $input['date'] = Carbon::createFromFormat('d/m/Y H:i', $input['date']);

        $event = $this->eventprovider->find($id);
        $this->checkEvent($event);

        $event->update($input);

        $this->session->flash('success', 'Your event has been updated successfully.');
        return Redirect::route('events.show', array('events' => $event->id));
    }

    /**
     * Delete an existing event.
     *
     * @param  int  $id
     * @return \Illuminate\Http\Response
     */
    public function destroy($id)
    {
        $event = $this->eventprovider->find($id);
        $this->checkEvent($event);

        $event->delete();

        $this->session->flash('success', 'Your event has been deleted successfully.');
        return Redirect::route('events.index');
    }

    /**
     * Check the event model.
     *
     * @param  mixed  $event
     * @return void
     */
    protected function checkEvent($event)
    {
        if (!$event) {
            throw new NotFoundHttpException('Event Not Found');
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
     * Return the event provider instance.
     *
     * @return \GrahamCampbell\BootstrapCMS\Providers\EventProvider
     */
    public function getEventProvider()
    {
        return $this->eventprovider;
    }
}