@extends('layouts.default')

@section('title')
{{{ $topic->title }}}_@parent
@stop

@section('description')
{{{ $topic->excerpt }}}
@stop

@section('content')

<div class="col-md-9 topics-show main-col">
  <!-- Topic Detial -->
  <div class="topic panel panel-default">
    <div class="infos panel-heading">

      <div class="pull-right avatar_large">
        <a href="{{ route('users.show', $topic->user->id) }}">
          <img src="{{ $topic->user->present()->gravatar }}" style="width:65px; height:65px;" class="img-thumbnail avatar" />
        </a>
      </div>

      <h1 class="panel-title topic-title">{{{ $topic->title }}}</h1>

      <div class="votes">

        <a data-ajax="post" href="javascript:void(0);" data-url="{{ route('topics.upvote', $topic->id) }}" title="{{ lang('Up Vote') }}" id="up-vote" class="vote {{ $currentUser && $topic->votes()->ByWhom(Auth::id())->WithType('upvote')->count() ? 'active' :'' }}">
            <li class="fa fa-chevron-up"></li> <span id="vote-count"> {{ $topic->vote_count }}</span>
        </a>
         &nbsp;
        <a data-ajax="post" href="javascript:void(0);" data-url="{{ route('topics.downvote', $topic->id) }}" title="{{ lang('Down Vote') }}" id="down-vote" class="vote {{ $currentUser && $topic->votes()->ByWhom(Auth::id())->WithType('downvote')->count() ? 'active' :'' }}">
            <li class="fa fa-chevron-down"></li>
        </a>
      </div>

      @include('topics.partials.meta')
    </div>

    <div class="content-body entry-content panel-body">

      @include('topics.partials.body', array('body' => $topic->body))

      <div data-lang-excellent="{{ lang('This topic has been mark as Excenllent Topic.') }}" data-lang-wiki="{{ lang('This is a Community Wiki.') }}" class="ribbon-container">
        @include('topics.partials.ribbon')
      </div>
    </div>

    <div class="appends-container" data-lang-append="{{ lang('Append') }}">
      @foreach ($topic->appends as $index => $append)

          <div class="appends">
              <span class="meta">{{ lang('Append') }} {{ $index }} &nbsp;·&nbsp; <abbr title="{{ $append->created_at }}" class="timeago">{{ $append->created_at }}</abbr></span>
              <div class="sep5"></div>
              <div class="markdown-reply append-content">
                  {!! $append->content !!}
              </div>
          </div>

      @endforeach
    </div>

    @include('topics.partials.topic_operate')
  </div>

  <!-- Reply List -->
  <div class="replies panel panel-default list-panel replies-index">
    <div class="panel-heading">
      <div class="total">{{ lang('Total Reply Count') }}: <b>{{ $replies->total() }}</b> </div>
    </div>

    <div class="panel-body">

      @if (count($replies))
        @include('topics.partials.replies')
        <div id="replies-empty-block" class="empty-block hide">{{ lang('No comments') }}~~</div>
      @else
        <ul class="list-group row"></ul>
        <div id="replies-empty-block" class="empty-block">{{ lang('No comments') }}~~</div>
      @endif

      <!-- Pager -->
      <div class="pull-right" style="padding-right:20px">
        {!! $replies->appends(Request::except('page'))->render() !!}
      </div>
    </div>
  </div>

  <!-- Reply Box -->
  <div class="reply-box form box-block">

    @include('layouts.partials.errors')

    <form method="POST" action="{{ route('replies.store') }}" accept-charset="UTF-8" id="reply-form">
      {!! csrf_field() !!}
      <input type="hidden" name="topic_id" value="{{ $topic->id }}" />

        @include('topics.partials.composing_help_block')

        <div class="form-group">
            @if ($currentUser)
                <textarea class="form-control" rows="5" placeholder="{{ lang('Please using markdown.') }}" style="overflow:hidden" id="reply_content" name="body" cols="50"></textarea>
            @else
                <textarea class="form-control" disabled="disabled" rows="5" placeholder="{{ lang('User Login Required for commenting.') }}" name="body" cols="50"></textarea>
            @endif
        </div>

        <div class="form-group status-post-submit">
            <input class="btn btn-primary {{ $currentUser ? :'disabled'}}" id="reply-create-submit" type="submit" value="{{ lang('Reply') }}">
            <span class="help-inline" title="Or Command + Enter">Ctrl+Enter</span>
        </div>

        <div class="box preview markdown-reply" id="preview-box" style="display:none;"></div>

    </form>
  </div>

</div>

@include('layouts.partials.sidebar')
@include('layouts.partials.bottombanner')
@stop