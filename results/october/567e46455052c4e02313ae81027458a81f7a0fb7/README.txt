commit 567e46455052c4e02313ae81027458a81f7a0fb7
Author: Luke Towers <github@luketowers.ca>
Date:   Tue Oct 11 17:15:40 2016 -0600

    Pass current model to record finder scope method

    This improves the extensibility of the record finder form widget by passing the current model to the query scope that will be applied to the records being displayed. It allows the use of attributes of the current model in the query scope applied to the records being displayed as options to select.

    In my use case, I have a main Survey model with related Field models. Field models can have parents and children for a tree structure, but I only want fields to have parents and children that are:
    **a) Not the main record itself**
    **and b) Members of / related to the same Survey model**

    By passing the current model to my query scope, I can filter out ineligible records like so:
    ```
    /**
     * Limit results to only records that are eligible to be parents of the provided model
     *
     * @param Query $query
     * @param Model $model The model to check for eligible parents agains
     * @return Query
     */
    public function scopeEligibleParents($query, $model) {
        return $query->where('id', '!=', $model->id)
                ->where('parent_id', '!=', $model->id)
                ->where('survey_id', '=', $model->survey_id);
    }
    ```