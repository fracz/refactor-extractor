commit 77d746a2f51c9a5cd2b5e593698654e7cd57904b
Author: David Mudrák <david@moodle.com>
Date:   Tue Mar 26 19:14:52 2013 +0100

    MDL-37602 Clean up and improve the overall feedback rendering

    In order to use the overall feedback in assessments of example
    submissions and in the assessment form preview, significant improvements
    in the rendering machinery were done.

    Workshop class provides two new methods overall_feedback_content_options()
    and overall_feedback_attachment_options() as they are needed at various
    scripts and libraries.

    Overall feedback is displayed as a part of the workshop_assessment_form
    only if the form is in editable mode (not frozen). If the form is
    displayed in read-only (frozen) mode, the caller is expected to render
    the overall feedback and list of attachments (the editor and filemanager
    elements do not support frozen mode). To do so, the renderable
    workshop_assessment now loads overall feedback data and provides two new
    methods get_overall_feedback_content() and
    get_overall_feedback_attachments() to be used by the renderer.

    Renderable workshop_submission, workshop_assessment and related classes
    now accept the workshop instance as the first parameter in their
    constructors. This way, these renderable classes have access to the
    workshop API.

    In the future, the rendering of submission files should be improved in
    the same way as is done in this patch (i.e. moving the logic and data
    preparation out of the renderer into the renderable classes).