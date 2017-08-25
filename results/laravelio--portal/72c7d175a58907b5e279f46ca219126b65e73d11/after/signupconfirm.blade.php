@extends('layouts._one_column')

@section('content')
    <section class="auth">
        <h1>We&#39;re going to create an account with this information.</h1>

        <div class="user">
            <img src="{{ $githubUser['avatar_url'] }}"/>
            <div class="bio">
                @if(isset($githubUser['login']))
                    <h2>{{ $githubUser['login'] }}</h2>
                @endif
                @if(isset($githubUser['email']))
                    <h3>{{ $githubUser['email'] }}</h3>
                @endif
                <a class="button" href="{{ action('AuthController@getSignupConfirmed') }}">Create My Laravel.IO Account</a>
            </div>
        </div>

    </section>
@stop