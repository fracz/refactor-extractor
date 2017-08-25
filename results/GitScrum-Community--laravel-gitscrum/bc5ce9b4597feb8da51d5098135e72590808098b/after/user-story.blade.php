<form action="{{route($route, ['slug'=>@$userStory->slug])}}" method="post" class="form-horizontal">
    {{ csrf_field() }}
    <div class="form-group">
        <label class="col-sm-12">{{_('Product Backlog')}}</label>
        <div class="col-sm-12">
            <select name="product_backlog_id" class="form-control m-b">
                @foreach ($productBacklogs as $productBacklog)
                <option value="{{$productBacklog->id}}"
                    @if ( @$productBacklog_id == $productBacklog->id ) selected="selected" @endif >
                        {{$productBacklog->title}}</option>
                @endforeach
            </select>
        </div>
    </div>
    <div class="hr-line-dashed"></div>
    <div class="form-group">
        <label class="col-sm-12">{{_('User Story')}}</label>
        <div class="col-sm-12">
            <textarea name="title" type="text" class="form-control" autocomplete="off" required>{{ @$userStory->title }}</textarea>
        </div>
    </div>
    <div class="form-group">
        <label class="col-sm-12">{{_('Description')}}</label>
        <div class="col-sm-12">
            <textarea name="description" type="text" class="form-control" required>{{ @$userStory->description }}</textarea>
            <span class="help-block m-b-none">A block of help text that breaks onto a new line and may extend beyond one line.</span>
        </div>
    </div>
    <div class="form-group">
        <label class="col-sm-12">{{_('Priority')}}</label>
        <div class="col-sm-12">
            <select name="config_priority_id" class="form-control m-b">
                @foreach ($priorities as $priority)
                <option value="{{$priority->id}}"
                    @if ( @$userStory->config_priority_id == $priority->id ) selected="selected" @endif >{{$priority->title}}</option>
                @endforeach
            </select>
        </div>
    </div>
    <div class="form-group">
        <label class="col-sm-12">{{_('Acceptance Criteria')}}</label>
        <div class="col-sm-12">
            <textarea name="acceptance_criteria" type="text" class="form-control" required>{{ @$userStory->acceptance_criteria }}</textarea>
            <span class="help-block m-b-none">A block of help text that breaks onto a new line and may extend beyond one line.</span>
        </div>
    </div>
    <div class="hr-line-dashed"></div>
    @include('partials.includes.form-btn-submit', ['action' => @$action])
</form>