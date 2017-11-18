/**
 * Copyright 2015-2017 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package zipkin.storage.elasticsearch.http;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import zipkin.storage.elasticsearch.http.internal.client.HttpCall;

import static zipkin.storage.elasticsearch.http.ElasticsearchHttpStorage.APPLICATION_JSON;

/** Ensures the index template exists and saves off the version */
final class EnsureIndexTemplate {

  /**
   * This is a blocking call, used inside a lazy. That's because no writes should occur until the
   * template is available.
   */
  static void apply(HttpCall.Factory callFactory, String name, String indexTemplate) {
    HttpUrl templateUrl = callFactory.baseUrl.newBuilder("_template").addPathSegment(name).build();
    Request getTemplate = new Request.Builder().url(templateUrl).tag("get-template").build();
    try {
      callFactory.execute(getTemplate, b -> null);
    } catch (IllegalStateException e) { // TODO: handle 404 slightly more nicely
      Request updateTemplate = new Request.Builder()
          .url(templateUrl)
          .put(RequestBody.create(APPLICATION_JSON, indexTemplate))
          .tag("update-template").build();
      callFactory.execute(updateTemplate, b -> null);
    }
  }
}