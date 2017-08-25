<!doctype html>
<html lang="{{ $app('i18n')->locale }}" data-base="@base('/')" data-route="@route('/')" data-version="{{ $app['cockpit/version'] }}" data-locale="{{ $app('i18n')->locale }}">
<head>
    <meta charset="UTF-8">
    <title>{{ $app['app.name'] }}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0" />

    <script>

        // App constants

        var SITE_URL = '{{ $app->pathToUrl('site:') }}';

    </script>

    {{ $app->assets($app('admin')->data->get('assets'), $app['cockpit/version']) }}

    <script>

        (function(App){

            App = App || {};

            App.$data = {{ json_encode($app('admin')->data->get('extract')) }};

        })(App);

    </script>

    @trigger('app.layout.header')
    @block('app.layout.header')

</head>
<body>

    <div class="app-header">

        <div class="app-header-topbar">

            <div class="uk-container uk-container-center">

                <div class="uk-grid">

                    <div  class="uk-flex-item-1">

                        <div class="uk-position-inline-block" data-uk-dropdown>

                            <strong class="uk-contrast">
                                <a href="#">
                                    <i class="uk-icon-bars"></i>
                                    <span>{{ $app['app.name'] }}</span>
                                </a>
                            </strong>

                            <div class="uk-dropdown app-panel-dropdown">

                                <div class="uk-grid uk-grid-gutter uk-grid-small uk-grid-divider">

                                    <div class="uk-grid-margin uk-width-medium-1-3">

                                        <div class="uk-margin">
                                            <span class="uk-badge uk-badge-primary">@lang('System')</span>
                                        </div>

                                        <ul class="uk-nav uk-nav-side uk-nav-dropdown">

                                            <li class="{{ $app['route'] == '/' ? 'uk-active':'' }}"><a href="@route('/')"><i class="uk-icon-justify uk-icon-dashboard"></i> @lang('Dashboard')</a></li>

                                            @hasaccess?('cockpit', 'manage.accounts')
                                            <li class="{{ strpos($app['route'],'/accounts')===0 ? 'uk-active':'' }}"><a href="@route('/accounts')"><i class="uk-icon-justify uk-icon-users"></i> @lang('Accounts')</a></li>
                                            @end

                                            @hasaccess?('cockpit', 'manage.media')
                                            <li class="{{ strpos($app['route'],'/finder')===0 ? 'uk-active':'' }}"><a href="@route('/finder')"><i class="uk-icon-justify uk-icon-folder"></i> @lang('Finder')</a></li>
                                            @end

                                            @hasaccess?('cockpit', 'manage.settings')
                                            <li class="uk-nav-divider"></li>
                                            <li class="{{ strpos($app['route'],'/settings')===0 ? 'uk-active':'' }}"><a href="@route('/settings')"><i class="uk-icon-justify uk-icon-cog"></i> @lang('Settings')</a></li>
                                            @end

                                        </ul>

                                        @trigger('cockpit.menu.aside')

                                    </div>

                                    <div class="uk-grid-margin uk-width-medium-2-3">

                                        <div class="uk-margin">
                                            <span class="uk-badge uk-badge-primary">@lang('Modules')</span>
                                        </div>

                                        @if($app('admin')->data['menu.modules']->count())
                                        <ul class="uk-sortable uk-grid uk-grid-small uk-grid-gutter uk-text-center" data-uk-sortable>

                                            @foreach($app('admin')->data['menu.modules'] as $item)
                                            <li class="uk-grid-margin uk-width-1-2 uk-width-medium-1-3">
                                                <a class="uk-display-block uk-panel-box {{ (@$item['active']) ? 'uk-bg-primary uk-contrast':'uk-panel-framed' }}" href="@route($item['route'])">
                                                    <div class="uk-text-large">
                                                        <i class="uk-icon-{{ isset($item['icon']) ? $item['icon']:'cube' }}"></i>
                                                    </div>
                                                    <div class="uk-text-truncate uk-text-small uk-margin-small-top">@lang($item['label'])</div>
                                                </a>
                                            </li>
                                            @endforeach

                                        </ul>
                                        @endif

                                        @trigger('cockpit.menu.main')

                                    </div>

                                </div>

                            </div>

                        </div>

                    </div>

                    <div>
                        @if($app('admin')->data['menu.modules']->count())
                        <ul class="uk-subnav uk-hidden-small">

                            @foreach($app('admin')->data['menu.modules'] as $item)
                            <li>
                                <a class="{{ (@$item['active']) ? 'uk-bg-primary uk-contrast':'uk-contrast' }}" href="@route($item['route'])" title="@lang($item['label'])" data-uk-tooltip="{offset:10}">
                                    <i class="uk-icon-{{ isset($item['icon']) ? $item['icon']:'cube' }}"></i>
                                </a>
                            </li>
                            @endforeach

                        </ul>
                        @endif
                    </div>

                </div>

            </div>

        </div>


        <nav class="uk-navbar">

            <div class="uk-container uk-container-center">

                <div class="uk-navbar-content uk-hidden-small" riot-mount>
                    <cp-search></cp-search>
                </div>

                <div class="uk-navbar-flip">

                    <div class="uk-navbar-content" data-uk-dropdown="{delay:150}">

                        <a href="@route('/accounts/account')" riot-mount>
                            <cp-gravatar email="{{ $app['user']['email'] }}" size="30" alt="{{ $app["user"]["name"] ? $app["user"]["name"] : $app["user"]["user"] }}"></cp-gravatar>
                        </a>

                        <div class="uk-dropdown uk-dropdown-navbar uk-dropdown-flip">
                            <ul class="uk-nav uk-nav-navbar">
                                <li class="uk-nav-header uk-text-truncate">{{ $app["user"]["name"] ? $app["user"]["name"] : $app["user"]["user"] }}</li>
                                <li><a href="@route('/accounts/account')">@lang('Account')</a></li>
                                <li class="uk-nav-divider"></li>
                                <li><a href="@route('/auth/logout')">@lang('Logout')</a></li>
                            </ul>
                        </div>

                    </div>
                </div>

            </div>
        </nav>
    </div>

    <div class="app-main">
        <div class="uk-container uk-container-center">
            {{ $content_for_layout }}
        </div>
    </div>

    <div class="app-footer">
        <div class="uk-container uk-container-center">

        </div>
    </div>

    @trigger('app.layout.footer')
    @block('app.layout.footer')


    <!-- RIOT COMPONENTS -->
    @foreach($app('admin')->data['components'] as $component)
    <script type="riot/tag" src="@base($component)"></script>
    @endforeach

</body>
</html>