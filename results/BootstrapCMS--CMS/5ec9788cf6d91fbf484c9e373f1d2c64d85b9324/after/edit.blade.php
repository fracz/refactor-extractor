@extends('layouts.default')

@section('title')
@parent
Edit {{ $post->getTitle() }}
@stop

@section('controls')

<div class="row-fluid">
    <div class="span12">
        <div class="span6">
            <p class="lead">
                Please edit the post:
            </p>
        </div>

        <div class="span6">
            <div class="pull-right">
                <a class="btn btn-success" href="{{ URL::route('blog.posts.show', array('posts' => $post->getId())) }}">Show Post</a> <a class="btn btn-danger action_confirm" href="{{ URL::route('blog.posts.destroy', array('posts' => $post->getId())) }}" data-token="{{ Session::getToken() }}" data-method="DELETE">Delete Post</a>
            </div>
        </div>
<hr>

@stop

@section('content')

<div class="well">
    <?php
    $form = array('url' => URL::route('blog.posts.update', array('posts' => $post->getId())),
        'method' => 'PATCH',
        'button' => 'Save Post',
        'defaults' => array(
            'title' => $post->getTitle(),
            'body' => $post->getBody(),
            ));
    ?>
    @include('posts.form')
</div>

@stop