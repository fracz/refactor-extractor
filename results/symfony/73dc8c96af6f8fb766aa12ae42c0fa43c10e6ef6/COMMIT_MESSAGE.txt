commit 73dc8c96af6f8fb766aa12ae42c0fa43c10e6ef6
Merge: 168a61b 07fa82d
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Jun 15 11:27:12 2011 +0200

    merged branch vicb/form-proto (PR #1315)

    Commits
    -------

    07fa82d [Form] Revert changes impacting perfomance and readability
    b709551 [Order] Make Form::types and FormView::types use the same order (Parent > Child)
    e56dad6 [Form] simplify the code
    bdd755e [Form] Fix the exception message when no block is found
    c68c511 [Form] Make theming form prototypes consistent (start by looking for a '_<id>_<section>' block)
    9ec9960 [Form] Simplify the code
    4e3e276 [Form] Make the prototype view child of the collection view

    Discussion
    ----------

    [Form] Make the prototype view child of the collection view

    This PR should be a base for discussion.

    The [current implementation](https://github.com/symfony/symfony/pull/1188) has some drawbacks because the prototype view is not a child of the collection view:

      * The 'multipart' attribute is not propagated from the prototype to the collection,
      * The prototype view do not use the theme from the collection.

    Those 2 points are fixed by the proposed implementation and one more benefit is that the template markup might be easier to work with:

    before:

    ```html
    <div id="form_emails">
      <div>
        <label for="form_emails_0">0</label>
        <input type="email" id="form_emails_0" name="form[emails][0]" value="a@b.com">
      </div>
      <script type="text/html" id="form_emails_prototype">
        <div>
          <label for="$$name$$">$$name$$</label>
          <input type="email" id="$$name$$" name="$$name$$" value="" />
        </div>
      </script>
    </div>
    ```
    after:

    ```html
    <div id="form_emails">
      <div>
        <label for="form_emails_0">0</label>
        <input type="email" id="form_emails_0" name="form[emails][0]" value="a@b.com">
      </div>
      <script type="text/html" id="form_emails_prototype">
        <div>
          <label for="form_emails_$$name$$">$$name$$</label>
          <input type="email" id="form_emails_$$name$$" name="form[emails][$$name$$]" value="" />
        </div>
      </script>
    </div>
    ```

    @kriswallsmith I'd like to get your feedback on this PR. thanks.

    ---------------------------------------------------------------------------

    by stof at 2011/06/14 07:01:01 -0700

    @fabpot any ETA about merging it ? Using the prototype currently is a pain to build the name. The change makes it far easier

    ---------------------------------------------------------------------------

    by fabpot at 2011/06/14 07:09:46 -0700

    The templates are much better but I'm a bit concerned that we need to add the logic into the Form class directly. That looks quite ugly. If there is no other way, I will merge it.

    ---------------------------------------------------------------------------

    by vicb at 2011/06/14 07:14:32 -0700

    I have found no better way... I am testing some minor tweaks I want to submit.

    ---------------------------------------------------------------------------

    by kriswallsmith at 2011/06/14 07:34:25 -0700

    I'm not happy with the code in Form.php either... would creating a PrototypeType accomplish the same thing?

    ---------------------------------------------------------------------------

    by vicb at 2011/06/14 07:42:07 -0700

    @kriswallsmith tried and dismissed, the id and name are bad & you have to go for `render_widget(form.get('proto'))` in the template. That should be fixeable but not any better.

    ---------------------------------------------------------------------------

    by kriswallsmith at 2011/06/14 07:45:21 -0700

    What do you mean the id and name are bad? If we have a distinct type for the prototype, can't we do whatever we want using buildView() and the template?

    ---------------------------------------------------------------------------

    by vicb at 2011/06/14 07:53:31 -0700

    @kriswallsmith the id would be smthg like `form_emails_$$name$$_prototype` but yes we should be able to do whatever we want but the code might end up being more complex.

    I am done with the tweaks but still open to feedback on this PR.

    ---------------------------------------------------------------------------

    by kriswallsmith at 2011/06/14 08:08:21 -0700

    Yes, that is the type of name I would expect.

    ---------------------------------------------------------------------------

    by kriswallsmith at 2011/06/14 08:08:33 -0700

    Oops -- I mean id.

    ---------------------------------------------------------------------------

    by kriswallsmith at 2011/06/14 08:09:42 -0700

    Maybe I'm confused what id you're referring to. I'll try to spend some time on this today.

    ---------------------------------------------------------------------------

    by vicb at 2011/06/14 08:23:56 -0700

    That should be the id of the `<input>`, the id of the script would be `form_emails_$$name$$_prototype_prototype` (if prototype is the name of the nested node).

    I am trying to setup a branch with my code (playing with git & netbeans local history)

    ---------------------------------------------------------------------------

    by vicb at 2011/06/14 08:46:25 -0700

    @kriswallsmith https://github.com/vicb/symfony/tree/kris/proto if that can help (there are still changes in Form.php)

    ---------------------------------------------------------------------------

    by kriswallsmith at 2011/06/14 08:47:08 -0700

    Thanks, I'll take a look.

    ---------------------------------------------------------------------------

    by vicb at 2011/06/15 00:48:38 -0700

    I would have expected it to be faster however `array_map` is about twice slower... reverted !