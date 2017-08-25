<div class="uk-grid-margin">

    <div class="uk-panel-box uk-panel-card">

        <div class="uk-panel-box-header">
            <strong>@lang('Regions')</strong>
            @if(count($regions))
            <span class="uk-badge">{{ count($regions) }}</span>
            @endif
        </div>

        @if(count($regions))

            <div class="uk-margin">

                <ul class="uk-list uk-margin-top">
                    @foreach(array_slice($regions, 0, count($regions) > 5 ? 5: count($regions)) as $region)
                    <li><a href="@route('/regions/form/'.$region['name'])"><i class="uk-icon-justify uk-icon-th"></i> {{ @$region['label'] ? $region['label'] : $region['name'] }}</a></li>
                    @endforeach
                </ul>

            </div>

            <div class="uk-panel-box-footer uk-bg-light">
                <a href="@route('/regions')">@lang('See all')</a>
            </div>

        @else

            <div class="uk-margin uk-text-center uk-text-muted">

                <p class="uk-text-large">
                    <i class="uk-icon-th"></i>
                </p>

                @lang('No regions'). <a href="@route('/regions/region')">@lang('Create a region')</a>.

            </div>

        @endif

    </div>

</div>