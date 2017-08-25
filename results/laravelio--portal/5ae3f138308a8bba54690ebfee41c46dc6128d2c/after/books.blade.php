@if(Uri::current() == '/')
    <section class="books">
@else
    <section class="books-with-border">
@endif

    <div class="words">
        <h1>Just Getting Started?</h1>
        <p>Sometimes you just want simple walkthroughs to assist you in learning a new framework. Follow along in building a simple application and explore many of Laravel's most powerful features with these books.</p>
    </div>

    <div class="book">
        <a href="https://leanpub.com/codehappy" target="_blank">
            <img src="{{ URL::to_asset('img/book-laravel-code-happy-dayle-rees.jpg') }}">
            <div class="title">Laravel: Code Happy</div>
            <div class="author">by Dayle Rees</div>
        </a>
    </div>

    <div class="book" target="_blank">
        <a href="http://www.packtpub.com/laravel-php-starter/book">
            <img src="{{ URL::to_asset('img/book-laravel-starter-shawn-mccool.jpg') }}">
            <div class="title">Laravel Starter</div>
            <div class="author">by Shawn McCool</div>
        </a>
    </div>

</section>