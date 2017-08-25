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

use Illuminate\Support\Facades\App;
use Illuminate\Support\Facades\Redirect;
use Illuminate\Support\Facades\Session;
use Illuminate\Support\Facades\Validator;
use GrahamCampbell\Binput\Facades\Binput;
use GrahamCampbell\Viewer\Facades\Viewer;
use GrahamCampbell\CMSCore\Models\Post;
use GrahamCampbell\CMSCore\Facades\PostProvider;
use GrahamCampbell\CMSCore\Controllers\AbstractController;

/**
 * This is the post controller class.
 *
 * @package    Bootstrap-CMS
 * @author     Graham Campbell
 * @copyright  Copyright (C) 2013  Graham Campbell
 * @license    https://github.com/GrahamCampbell/Bootstrap-CMS/blob/develop/LICENSE.md
 * @link       https://github.com/GrahamCampbell/Bootstrap-CMS
 */
class PostController extends AbstractController
{
    /**
     * Constructor (setup access permissions).
     *
     * @return void
     */
    public function __construct()
    {
        $this->setPermissions(array(
            'create'  => 'blog',
            'store'   => 'blog',
            'edit'    => 'blog',
            'update'  => 'blog',
            'destroy' => 'blog',
        ));

        parent::__construct();
    }

    /**
     * Display a listing of the posts.
     *
     * @return \Illuminate\Http\Response
     */
    public function index()
    {
        $posts = PostProvider::paginate();
        $links = PostProvider::links();

        return Viewer::make('posts.index', array('posts' => $posts, 'links' => $links));
    }

    /**
     * Show the form for creating a new post.
     *
     * @return \Illuminate\Http\Response
     */
    public function create()
    {
        return Viewer::make('posts.create');
    }

    /**
     * Store a new post.
     *
     * @return \Illuminate\Http\Response
     */
    public function store()
    {
        $input = array(
            'title'   => Binput::get('title'),
            'summary' => Binput::get('summary'),
            'body'    => Binput::get('body'),
            'user_id' => $this->getUserId(),
        );

        $rules = Post::$rules;

        $val = Validator::make($input, $rules);
        if ($val->fails()) {
            return Redirect::route('blog.posts.create')->withInput()->withErrors($val->errors());
        }

        $post = PostProvider::create($input);

        Session::flash('success', 'Your post has been created successfully.');
        return Redirect::route('blog.posts.show', array('posts' => $post->getId()));
    }

    /**
     * Show the specified post.
     *
     * @param  int  $id
     * @return \Illuminate\Http\Response
     */
    public function show($id)
    {
        $post = PostProvider::find($id);
        $this->checkPost($post);

        $comments = $post->getComments();

        return Viewer::make('posts.show', array('post' => $post, 'comments' => $comments));
    }

    /**
     * Show the form for editing the specified post.
     *
     * @param  int  $id
     * @return \Illuminate\Http\Response
     */
    public function edit($id)
    {
        $post = PostProvider::find($id);
        $this->checkPost($post);

        return Viewer::make('posts.edit', array('post' => $post));
    }

    /**
     * Update an existing post.
     *
     * @param  int  $id
     * @return \Illuminate\Http\Response
     */
    public function update($id)
    {
        $input = array(
            'title'   => Binput::get('title'),
            'summary' => Binput::get('summary'),
            'body'    => Binput::get('body', null, true, false), // no xss protection please
        );

        $rules = Post::$rules;
        unset($rules['user_id']);

        $val = Validator::make($input, $rules);
        if ($val->fails()) {
            return Redirect::route('blog.posts.edit', array('posts' => $id))->withInput()->withErrors($val->errors());
        }

        $post = PostProvider::find($id);
        $this->checkPost($post);

        $post->update($input);

        Session::flash('success', 'Your post has been updated successfully.');
        return Redirect::route('blog.posts.show', array('posts' => $post->getId()));
    }

    /**
     * Delete an existing post.
     *
     * @param  int  $id
     * @return \Illuminate\Http\Response
     */
    public function destroy($id)
    {
        $post = PostProvider::find($id);
        $this->checkPost($post);

        $post->delete();

        Session::flash('success', 'Your post has been deleted successfully.');
        return Redirect::route('blog.posts.index');
    }

    /**
     * Check the post model.
     *
     * @param  mixed  $post
     * @return void
     */
    protected function checkPost($post)
    {
        if (!$post) {
            return App::abort(404, 'Post Not Found');
        }
    }
}