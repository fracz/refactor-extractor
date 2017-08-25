{{ $app->assets(['assets:lib/uikit/js/components/form-password.min.js']) }}

<div>
    <ul class="uk-breadcrumb">
        <li><a href="@route('/settings')">@lang('Settings')</a></li>
        <li><a href="@route('/accounts')">@lang('Accounts')</a></li>
        <li class="uk-active"><span>@lang('Account')</span></li>
    </ul>
</div>

<div class="uk-grid uk-margin-top uk-invisible" data-uk-grid-margin riot-view>

    <div class="uk-width-medium-2-4">

        <h3>@lang('Profile')</h3>

        <div class="uk-panel">

            <div class="uk-grid" data-uk-grid-margin>

                <div class="uk-width-medium-1-1">

                    <form id="account-form" class="uk-form" onsubmit="{ submit }">


                        <div class="uk-form-row">
                            <label class="uk-text-small">@lang('Name')</label>
                            <input class="uk-width-1-1 uk-form-large" type="text" bind="account.name" autocomplete="off" required>
                        </div>

                        <div class="uk-form-row">
                            <label class="uk-text-small">@lang('Username')</label>
                            <input class="uk-width-1-1 uk-form-large" type="text" bind="account.user" autocomplete="off" required>
                        </div>

                        <div class="uk-form-row">
                            <label class="uk-text-small">@lang('Email')</label>
                            <input class="uk-width-1-1 uk-form-large" type="email" bind="account.email" autocomplete="off" required>
                        </div>

                        <div class="uk-form-row">
                            <label class="uk-text-small">@lang('New Password')</label>
                            <div class="uk-form-password uk-width-1-1">
                                <input class="uk-form-large uk-width-1-1" type="password" placeholder="@lang('Password')" bind="account.password" autocomplete="off">
                                <a href="" class="uk-form-password-toggle" data-uk-form-password>@lang('Show')</a>
                            </div>
                            <div class="uk-alert">
                                @lang('Leave the password field empty to keep your current password.')
                            </div>
                        </div>

                        @trigger('cockpit.account.editview')

                        <div class="uk-form-row">
                            <button class="uk-button uk-button-large uk-button-primary uk-width-1-3 uk-margin-small-right">@lang('Save')</button>
                            <a href="@route('/accounts')">@lang('Cancel')</a>
                        </div>

                    </form>

                </div>

            </div>
        </div>

    </div>

    <div class="uk-width-medium-1-4 uk-form">

        <h3>@lang('Settings')</h3>

        @if($app["user"]["group"]=="admin" AND @$account["_id"]!=$app["user"]["_id"])
        <div class="uk-form-row">
            <label class="uk-text-small">@lang('Status')</label>

            <div class="uk-form-controls uk-margin-small-top">
                <a class="uk-button { !account.active ? 'uk-button-danger':'uk-button-success' }" onclick="{ toggleactive }">
                    { account.active ? 'Active' : 'Inactive' }
                </a>
            </div>

        </div>
        @endif

        <div class="uk-form-row">
            <label class="uk-text-small">@lang('Language')</label>

            <div class="uk-form-controls uk-margin-small-top">
                <div class="uk-form-select">
                    <a>{ account.i18n }</a>
                    <select class="uk-width-1-1 uk-form-large" name="i18n">
                        @foreach($languages as $lang)
                        <option value="{{ $lang['i18n'] }}">{{ $lang['language'] }}</option>
                        @endforeach
                    </select>
                </div>
            </div>
        </div>

        @if($app["user"]["group"]=="admin" AND @$account["_id"]!=$app["user"]["_id"])
        <div class="uk-form-row">
            <label class="uk-text-small">@lang('Group')</label>

            <div class="uk-form-controls uk-margin-small-top">
                <div class="uk-form-select">
                    <a>{ account.group }</a>
                    <select class="uk-width-1-1 uk-form-large" name="group">
                        @foreach($groups as $group)
                        <option value="{{ $group }}">{{ $group }}</option>
                        @endforeach
                    </select>
                </div>
            </div>

        </div>
        @endif

    </div>

    <script type="view/script">

        var $this = this;

        this.account = {{ json_encode($account) }};

        riot.util.bind(this);

        this.on('mount', function(){
            this.root.classList.remove('uk-invisible');
        });

        toggleactive() {
            this.account.active = !(this.account.active);
        }

        submit() {

            App.request("/accounts/save", {"account": this.account}).then(function(data){
                $this.account = data;
                App.ui.notify("Account saved", "success");
            });

            return false;
        }

    </script>

</div>