commit 92823b863555d24dd3a800740296e06432a84a1b
Author: Daryl Koopersmith <dkoopersmith@gmail.com>
Date:   Sun Nov 4 22:59:12 2012 +0000

    Streamlining media, part I.

    The main goal here is to rearrange the media components in a modularized structure to support more linear workflows. This is that structure using the pre-existing workflows, which will be improved over the course of the next few commits.

    This leaves a few pieces a bit rough around the edges: namely gallery editing and selecting a featured image.

    The fine print follows.

    ----

    '''Styles'''
    * Tightened padding around the modal to optimize for a smaller default screen size.
    * Added a light dashed line surrounding the modal to provide a subtle cue for the persistent dropzone (which is evolving into a power user feature since we now have a dedicated `upload` state).
    * Add a size for `hero` buttons.
    * Remove transitions from frame subviews (e.g. menu, content, sidebar, toolbar).

    ----

    '''Code'''

    `wp.media.controller.StateManager`
    * Don't fire `activate` and `deactivate` if attempting to switch to the current state.

    `wp.media.controller.State`
    * Add a base state class to bind default methods (as not all states will inherit from the `Library` state).
    * On `activate`, fire `activate()`, `menu()`, `content()`, `sidebar()`, and `toolbar()`.
    * The menu view is often a shared object (as its most common use case is switching between states). Assign the view to the state's `menu` attribute.
    * `menu()` automatically fetches the state's `menu` attribute, attaches the menu view to the frame, and attempts to select a menu item that matches the state's `id`.

    `wp.media.controller.Library`
    * Now inherits from `wp.media.controller.State`.

    `wp.media.controller.Upload`
    * A new state to improve the upload experience.
    * Displays a large dropzone when empty (a `UploaderInline` view).
    * When attachments are uploaded, displays management interface (a `library` state restricted to attachments uploaded during the current session).

    `wp.media.view.Frame`
    * In `menu()`, `content()`, `sidebar()`, and `toolbar()`, only change the view if it differs from the current view. Also, ensure `hide-*` classes are properly removed.
    *

    `wp.media.view.PriorityList`
    * A new container view used to sort and render child views by the `priority` property.
    * Used by `wp.media.view.Sidebar` and `wp.media.view.Menu`.
    * Next step: Use two instances to power `wp.media.view.Toolbar`.

    `wp.media.view.Menu` and `wp.media.view.MenuItem`
    * A new `PriorityList` view that renders a list of views used to switch between states.
    * `MenuItem` instances have `id` attributes that are tied directly to states.
    * Separators can be added as plain `Backbone.View` instances with the `separator` class.
    * Supports any type of `Backbone.View`.

    `media.view.Menu.Landing`
    * The landing menu for the 'insert media' workflow.
    * Includes an inactive link to an "Embed from URL" state.
    * Next steps: only use in select cases to allot for other workflows (such as featured images).

    `wp.media.view.AttachmentsBrowser`
    * A container to render an `Attachments` view with accompanying UI controls (similar to what the `Attachments` view was when it contained the `$list` property).
    * Currently only renders a `Search` view as a control.
    * Next steps: Add optional view counts (e.g. "21 images"), upload buttons, and collection filter UI.

    `wp.media.view.Attachments`
    * If the `Attachments` scroll buffer is not filled with `Attachment` views, continue loading more attachments.
    * Use `this.model` instead of `this.controller.state()` to allow `Attachments` views to have differing `edge` and `gutter` properties.
    * Add `edge()`, a method used to calculate the optimal dimensions for an attachment based on the current width of the `Attachments` container element.
    * `edge()` is currently only enabled on resize, as the relative positioning and CSS transforms used to center thumbnails are suboptimal when coupled with frequent resizing.
    * Next steps: For infinite scroll performance improvements, look into absolutely positioning attachment views and paging groups of attachment views.

    `wp.media.view.UploaderWindow`
    * Now generates a `$browser` element as the browse button (instead of a full `UploaderInline` view). Using a portable browse button prevents us from having to create a new `wp.Uploader` instance every time we want access to a browse button.

    `wp.media.view.UploaderInline`
    * No longer directly linked to the `UploaderWindow` view or its `wp.Uploader` instance.
    * Used as the default `upload` state view.

    `wp.media.view.Selection`
    * An interactive representation of the selected `Attachments`.
    * Based on the improved workflows, this is likely overkill. For simplicity's sake, will probably remove this in favor of `SelectionPreview`.

    ----

    see #21390.



    git-svn-id: http://core.svn.wordpress.org/trunk@22362 1a063a9b-81f0-0310-95a4-ce76da25c4cd