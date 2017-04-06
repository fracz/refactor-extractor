commit 5f70d615a5f7e102424c6adc15d7a6f697870b6e
Author: Vojta Jina <vojta.jina@gmail.com>
Date:   Fri Mar 30 13:40:19 2012 -0700

    refactor(ngInclude): remove scope attribute

    The purpose of allowing the scope to be specified was to enable the $route service to work
    together with ngInclude. However the functionality of creating scopes was in the recent past
    moved from the $route service to the ngView directive, so currently there is no valid use case
    for specifying the scope for ngInclude. In fact, allowing the scope to be defined can under
    certain circumstances lead to memory leaks.

    Breaks ngInclude does not have scope attribute anymore.