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
use Response;
use Validator;

use Binput;
use HTMLMin;

use CommentProvider;
use PostProvider;
use GrahamCampbell\CMSCore\Models\Comment;

class CommentController extends BaseController {

    /**
     * Constructor (setup access permissions).
     *
     * @return void
     */
    public function __construct() {
        $this->setPermissions(array(
            'store'   => 'user',
            'update'  => 'mod',
            'destroy' => 'mod',
        ));

        parent::__construct();
    }

    /**
     * Display a listing of the comments.
     *
     * @return \Illuminate\Http\Response
     */
    public function index($post_id) {
        $this->checkAjax();

        $post = PostProvider::find($post_id, array('id'));
        $this->checkPost($post);

        $comments = $post->getComments('id', 'version');

        return Response::json($comments->toArray());
    }

    /**
     * Store a new comment.
     *
     * @return \Illuminate\Http\Response
     */
    public function store($post_id) {
        $this->checkAjax();

        $input = array(
            'body'    => Binput::get('body'),
            'user_id' => $this->getUserId(),
            'post_id' => $post_id
        );

        $rules = Comment::$rules;

        $val = Validator::make($input, $rules);
        if ($val->fails()) {
            App::abort(400, 'Your comment was empty.');
        }

        $comment = CommentProvider::create($input);

        return Response::json(array('success' => true, 'msg' => 'Comment created successfully.', 'contents' => HTMLMin::make('posts.comment', array('comment' => $comment, 'post_id' => $comment->getPostId())), 'comment_id' => $comment->getId()));
    }

    /**
     * Show the specified post.
     *
     * @param  int  $id
     * @return \Illuminate\Http\Response
     */
    public function show($post_id, $id) {
        $this->checkAjax();

        $comment = CommentProvider::find($id);
        $this->checkComment($comment);

        return Response::json(array('contents' => nl2br(e($comment->getBody())), 'comment_id' => $id));
    }

    /**
     * Update an existing comment.
     *
     * @param  int  $id
     * @return \Illuminate\Http\Response
     */
    public function update($post_id, $id) {
        $this->checkAjax();

        $input = array('body' => Binput::get('value'));

        $rules = array('body' => Comment::$rules['body']);

        $val = Validator::make($input, $rules);
        if ($val->fails()) {
            App::abort(400, 'The comment was empty.');
        }

        $comment = CommentProvider::find($id);
        $this->checkComment($comment);

        $version = Binput::get('version');
        if ($version != $comment->getVersion) {
            App::abort(409, 'The comment was modified by someone else.');
        }

        $comment->update(array_merge($input, array('version' => $version+1)));

        return Response::json(array('success' => true, 'msg' => 'Comment updated successfully.', 'contents' => nl2br(e($comment->getBody())), 'comment_id' => $id));
    }

    /**
     * Delete an existing comment.
     *
     * @param  int  $id
     * @return \Illuminate\Http\Response
     */
    public function destroy($post_id, $id) {
        $this->checkAjax();

        $comment = CommentProvider::find($id);
        $this->checkComment($comment);

        $comment->delete();

        return Response::json(array('success' => true, 'msg' => 'Comment deleted successfully.', 'comment_id' => $id));
    }

    /**
     * Check the comment model.
     *
     * @return mixed
     */
    protected function checkComment($comment) {
        if (!$comment) {
            return App::abort(404, 'Comment Not Found');
        }
    }

    /**
     * Check the post model.
     *
     * @return mixed
     */
    protected function checkPost($post) {
        if (!$post) {
            return App::abort(404, 'Post Not Found');
        }
    }
}