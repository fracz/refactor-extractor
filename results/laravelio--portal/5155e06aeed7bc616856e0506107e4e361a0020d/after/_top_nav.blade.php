<header class="top-navigation">
    <div class="top-navigation-logo">
        <a href="{{ action('HomeController@getIndex') }}">
            <img class="logo" src="/images/laravel-io-logo.png">
        </a>
    </div>
    <nav>
        <ul>
            <li>
                <a class="{{ Request::is('article*') ? 'active' : null }}" href="{{ action('ArticlesController@getIndex') }}">Articles</a>
            </li>
            <li>
                <a class="{{ Request::is('forum*') ? 'active' : null }}" href="{{ action('ForumThreadsController@getIndex') }}">Forum</a>
            </li>
            <li>
                <a class="{{ Request::is('chat*') ? 'active' : null }}" href="{{ action('ChatController@getIndex') }}">Live Chat</a>
            </li>
        </ul>
    </nav>
    <ul class="user-navigation">
        @if(Auth::check())
            {{-- <li><a href="{{ action('DashboardController@getIndex') }}">{{ Auth::user()->name }}<span class="dashboard-word">'s Dashboard</span></a></li> --}}
            <li><a class="button" href="{{ action('AuthController@getLogout') }}">Logout</a></li>
        @else
            <li><a class="button" href="{{ action('AuthController@getLogin') }}">Login with GitHub</a></li>
        @endif
    </ul>
</header>