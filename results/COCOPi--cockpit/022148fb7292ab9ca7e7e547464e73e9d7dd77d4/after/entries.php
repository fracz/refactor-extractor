<div class="uk-margin">

    <div class="uk-form-icon uk-form uk-width-1-1 uk-text-muted">

        <i class="uk-icon-filter"></i>
        <input class="uk-width-1-1 uk-form-large uk-form-blank" type="text" name="txtfilter" placeholder="@lang('Filter items...')" onchange="{ updatefilter }">

    </div>

</div>

<div class="uk-alert" if="{ !entries.length && filter }">
    @lang('No entries found').
</div>



<table class="uk-table uk-table-striped uk-margin-top" if="{ entries.length }">
    <thead>
        <tr>
            <th width="20"><input type="checkbox" data-check="all"></th>
            <th each="{field,idx in fields}">
                <a class="uk-link-muted { parent.sort[field.name] ? 'uk-text-primary':'' }" onclick="{ parent.updatesort }" data-sort="{ field.name }">

                    { field.label || field.name }

                    <span if="{parent.sort[field.name]}" class="uk-animation-fade uk-icon-long-arrow-{ parent.sort[field.name] == 1 ? 'up':'down'}"></span>
                </a>
            </th>
            <th width="20"></th>
        </tr>
    </thead>
    <tbody>
        <tr each="{entry,idx in entries}">
            <td><input type="checkbox" data-check data-id="{ entry._id }"></td>
            <td class="uk-text-truncate" each="{field,idy in parent.fields}" if="{ field.name != '_modified' }">
                <a class="uk-link-muted" href="@route('/collections/entry/'.$collection['name'])/{ parent.entry._id }">
                    { String(parent.entry[field.name] === undefined ? '': parent.entry[field.name]) }
                </a>
            </td>
            <td>{ (new Intl.DateTimeFormat()).format( new Date( 1000 * entry._modified )) }</td>
            <td>
                <span class="uk-float-right" data-uk-dropdown="\{mode:'click'\}">

                    <a class="uk-icon-bars"></a>

                    <div class="uk-dropdown uk-dropdown-flip">
                        <ul class="uk-nav uk-nav-dropdown">
                            <li class="uk-nav-header">@lang('Actions')</li>
                            <li><a href="@route('/collections/entry/'.$collection['name'])/{ entry._id }">@lang('Edit')</a></li>
                            <li><a onclick="{ parent.remove }">@lang('Delete')</a></li>
                        </ul>
                    </div>
                </span>
            </td>
        </tr>
    </tbody>
</table>



<div class="uk margin" if="{ loadmore }">
    <a class="uk-button uk-width-1-1" onclick="{ load }">
        @lang('Load more..')
    </a>
</div>