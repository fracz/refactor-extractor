commit e717c99f3bb92a7215221c69277b87f981e7ce75
Merge: 6454952 2c1108c
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Jun 22 09:12:24 2011 +0200

    merged branch vicb/form/theming (PR #1369)

    Commits
    -------

    2c1108c [Form] Revert the ability to override anything else than the text of the label while rendering a row
    da467a6 [Form] Fix the exception message when no block is found while rendering
    8670995 [Form] Optimize rendering when the block to render is known
    41e07c9 [Form] Optimize rendering
    ee5d975 [Form] Remove a test which is no more relevant (after recent FileType refactoring)
    f729c6b [Form] Add the ability to override label & widget options when rendering a row
    e09ae3f [Form][FrameworkBundle] Make FormHelper::renderSection() recursively callable, introduce FormHelper::renderBlock()
    e43fb98 [Form][TwigBridge] Make FormExtension::render() recursively callable to ease theming

    Discussion
    ----------

    [Form] Some refactoring of the rendering

    # First two commits

    ## FormExtension::render() can now be called recursively.

    The main use case is theming support in for collections. Let's consider that you have a collection of `CustomType`, the type hierarchy while rendering the proto would be `field < form < custom < prototype`. Before this change any theme applied to your custom type (i.e. a `custom_row` block) would not have been taken into account while rendering the prototype because of the structure of the `prototype_row` block:

    ```html
    {% block prototype_row %}
    {% spaceless %}
        <script type="text/html" id="{{ proto_id }}">{{ block('field_row') }}</script>
    {% endspaceless %}
    {% endblock prototype_row %}
    ```
    which skip the `custom_row` block rendering to fallback to the `field_row` block rendering.

    With this PR `prototype_row` recursively calls `FormExtension::render()`

    ```html
    {% block prototype_row %}
    {% spaceless %}
        <script type="text/html" id="{{ proto_id }}">{{ form_row(form) }}</script>
    {% endspaceless %}
    {% endblock prototype_row %}
    ```

    this has for effect to render the block for the parent type (i.e. `custom_row`)

    ## FormHelper

    The `FormHelper` has been updated to more closely match the `FormExtension` architecture and the templates have been modified accordingly. `echo $view['form']->renderBlock(<block name>)` is the php equivalent of `{{ block(<block name>) }}`.

    The attributes are now rendered using a template rather than by the `FormHelper::attributes()` method.

    Several templates have been fixed.

    # Third commit

    The `$varStack` property was used to forward options to the label and the widget when rendering a row. The implementation was not working as expected. The proposed way to override label and widget options is to pass these options in the `label` and `widget` keys while callinf `render_row`.

    That would be:
    `{{ form_row(form.field, {"attr": {<row attributes>}, "label" : {"label": <text>, "attr": {<label attr>}}, "widget" : { "attr" : {<widget attributes}} } }}`

    So there is now the ability to set attributes for the row (`<div>` or `<tr>`).

    This has been discussed on [the mailing list](http://groups.google.com/group/symfony-devs/browse_thread/thread/17754128ba480545). **I would like to find a compromise with @Seldaek before this gets merged**

    The `$varStack` property is now only used when recursively calling `FormExtension::render()`

    # Notes

    I have preferred to submit several commits in order to ease review and to keep some history.

    ---------------------------------------------------------------------------

    by stof at 2011/06/20 05:20:56 -0700

    @vicb On a side note, do you think it would be possible to support form theming in PHP templates too ? Currently, the only way to customize the rendering of forms when using PHP templates is to overwrite the FrameworkBundle's templates, and this impacts all forms. This makes the PHP rendering far less powerful than the Twig one.
    I don't know the Form rendering and the PHPEngine well enough to know if it is feasible for 2.1 or not.

    ---------------------------------------------------------------------------

    by vicb at 2011/06/20 05:35:11 -0700

    @stof I hope to make it possible but I need a little bit more thinking to find the best possible solution which should not look like a hack.

    ---------------------------------------------------------------------------

    by vicb at 2011/06/21 01:13:10 -0700

    This should not be merged yet, it might have some issue with the variable stack. I am working on it.

    ---------------------------------------------------------------------------

    by vicb at 2011/06/21 01:41:11 -0700

    Sorted out the issue, it was linked to some local _optimization_, the code of this PR is ok.

    ---------------------------------------------------------------------------

    by vicb at 2011/06/21 02:01:24 -0700

    I have pushed a [POC of php theming based on this PR](https://github.com/vicb/symfony/commits/form%2Fphp-theme) to my repo - it is lacking a configuration and cache layer.

    I have open [a thread on the ml](http://groups.google.com/group/symfony-devs/browse_thread/thread/9b3f131fe116b511) to discuss this.

    ---------------------------------------------------------------------------

    by vicb at 2011/06/21 23:40:21 -0700

    @fabpot fixed in the last commit.