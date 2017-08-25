commit 668247876b0f2d4fce10625eb703758e8d422eb2
Author: Mat Whitney <mwhitney@mail.sdsu.edu>
Date:   Wed May 15 10:12:13 2013 -0700

    Update user_model for compatibility with MY_Model changes in e77ea0aa9be4f48cf38cd2c5f5d81e52b15429a1

    Additionally:
    - cleaned up PHPDoc comments in MY_Model.php
    - removed unused $return_type argument from BF_Model::find() method
    - refactored BF_Model::delete() and BF_Model::delete_where() to show the
    similarity in their logic (should probably make one a wrapper of the
    other)
    - added @internal note to BF_Model::count_all() regarding potential
    confusion with $this->db->count_all()