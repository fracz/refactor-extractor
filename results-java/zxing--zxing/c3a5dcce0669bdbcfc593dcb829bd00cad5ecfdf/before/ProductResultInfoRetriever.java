/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android.result.supplement;

import android.content.Context;
import android.os.Handler;
import android.widget.TextView;
import com.google.zxing.client.android.R;
import com.google.zxing.client.android.history.HistoryManager;
import com.google.zxing.client.android.LocaleManager;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ProductResultInfoRetriever extends SupplementalInfoRetriever {

  private static final String BASE_PRODUCT_URI =
      "http://www.google." + LocaleManager.getProductSearchCountryTLD() +
          "/m/products?ie=utf8&oe=utf8&scoring=p&source=zxing&q=";
  private static final Pattern PRODUCT_NAME_PRICE_PATTERN =
      Pattern.compile("owb63p\">([^<]+).+zdi3pb\">([^<]+)");


  private final String productID;
  private final String source;

  ProductResultInfoRetriever(TextView textView,
                             String productID,
                             Handler handler,
                             HistoryManager historyManager,
                             Context context) {
    super(textView, handler, historyManager);
    this.productID = productID;
    this.source = context.getString(R.string.msg_google_product);
  }

  @Override
  void retrieveSupplementalInfo() throws IOException, InterruptedException {

    String encodedProductID = URLEncoder.encode(productID, "UTF-8");
    String uri = BASE_PRODUCT_URI + encodedProductID;
    String content = downloadViaHttp(uri);

    Matcher matcher = PRODUCT_NAME_PRICE_PATTERN.matcher(content);
    if (matcher.find()) {
      append(productID, source, new String[] { matcher.group(1), matcher.group(2) }, uri);
    }
  }

}