<div class="col-md-3 side-bar">

    @if (isset($topic))
  <div class="panel panel-default corner-radius">

      <div class="panel-heading text-center">
        <h3 class="panel-title">作者：{{ $topic->user->name }}</h3>
      </div>

    <div class="panel-body text-center topic-author-box">
        @include('topics.partials.topic_author_box')
    </div>
  </div>
  @endif


  @if (isset($userTopics) && count($userTopics))
  <div class="panel panel-default corner-radius">
    <div class="panel-heading text-center">
      <h3 class="panel-title">{{ $topic->user->name }} 的其他话题</h3>
    </div>
    <div class="panel-body">
      @include('layouts.partials.sidebar_topics', ['sidebarTopics' => $userTopics])
    </div>
  </div>
  @endif


  @if (isset($categoryTopics) && count($categoryTopics))
  <div class="panel panel-default corner-radius">
    <div class="panel-heading text-center">
      <h3 class="panel-title">{{ lang('Same Category Topics') }}</h3>
    </div>
    <div class="panel-body">
      @include('layouts.partials.sidebar_topics', ['sidebarTopics' => $categoryTopics])
    </div>
  </div>
  @endif

  <div class="panel panel-default corner-radius">

    @if (isset($category))
      <div class="panel-heading text-center">
        <h3 class="panel-title">{{{ $category->name }}}</h3>
      </div>
    @endif

    <div class="panel-body text-center">
      <div class="btn-group">
        <a href="{{ isset($category) ? URL::route('topics.create', ['category_id' => $category->id]) : URL::route('topics.create') }}" class="btn btn-primary btn-lg">
          <i class="fa fa-paint-brush" aria-hidden="true"></i> {{ lang('New Topic') }}
        </a>
      </div>
    </div>
  </div>

@if (Route::currentRouteName() == 'topics.index')

<div class="panel panel-default corner-radius panel-hot-topics">
  <div class="panel-heading text-center">
    <h3 class="panel-title">{{ lang('Hot Topics') }}</h3>
  </div>
  <div class="panel-body">
    @include('layouts.partials.sidebar_topics', ['sidebarTopics' => $hot_topics])
  </div>
</div>

@endif

  <div class="panel panel-default corner-radius">
    <div class="panel-body text-center" style="padding: 7px; padding-top: 8px;">
      <a href="http://www.ucloud.cn/site/seo.html?utm_source=zanzhu&utm_campaign=phphub&utm_medium=display&utm_content=shengji&ytag=phphubshenji" target="_blank" rel="nofollow" title="" style="line-height: 66px;">
        <img src="http://ww1.sinaimg.cn/large/6d86d850jw1f2xfmssojsj20dw03cjs5.jpg" width="100%">
      </a>
  </div>
  </div>

  @if (isset($links) && count($links))
    <div class="panel panel-default corner-radius">
      <div class="panel-heading text-center">
        <h3 class="panel-title">{{ lang('Links') }}</h3>
      </div>
      <div class="panel-body text-center" style="padding-top: 5px;">
        @foreach ($links as $link)
            <a href="{{ $link->link }}" target="_blank" rel="nofollow" title="{{ $link->title }}" style="padding: 3px;">
                <img src="{{ $link->cover }}" style="width:150px; margin: 3px 0;">
            </a>
        @endforeach
      </div>
    </div>
  @endif

@if (isset($randomExcellentTopics) && count($randomExcellentTopics))

<div class="panel panel-default corner-radius panel-hot-topics">
  <div class="panel-heading text-center">
    <h3 class="panel-title">{{ lang('Recommend Topics') }}</h3>
  </div>
  <div class="panel-body">
    @include('layouts.partials.sidebar_topics', ['sidebarTopics' => $randomExcellentTopics])
  </div>
</div>

@endif

@if (Route::currentRouteName() == 'topics.index')

  <div class="panel panel-default corner-radius panel-active-users">
    <div class="panel-heading text-center">
      <h3 class="panel-title">{{ lang('Active Users') }}</h3>
    </div>
    <div class="panel-body">
      @include('topics.partials.active_users')
    </div>
  </div>

<div class="panel panel-default corner-radius">
  <div class="panel-heading text-center">
    <h3 class="panel-title">{{ lang('App Download') }}</h3>
  </div>
  <div class="panel-body text-center" style="padding: 7px;">
    <a href="https://phphub.org/topics/1531" target="_blank" rel="nofollow" title="">
      <img src="https://dn-phphub.qbox.me/uploads/images/201512/08/1/cziZFHqkm8.png" style="width:240px;">
    </a>
  </div>
</div>

@endif

</div>
<div class="clearfix"></div>