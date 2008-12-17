/*
 * Copyright (C) 2008 ZXing authors
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

package com.google.zxing.web.generator.client;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * A generator for geo location. It also accepts a google maps links and
 * extracts the coordinates and query from the URL.
 * 
 * @author Yohann Coppel
 */
public class GeoLocationGenerator implements GeneratorSource {
  private static final String LON_REGEXP = "[+-]?[0-9]+(.[0-9]+)?";
  private static final String LAT_REGEXP = "[+-]?[0-9]+(.[0-9]+)?";
  
  Grid table = null;
  TextBox latitude = new TextBox();
  TextBox longitude = new TextBox();
  TextBox query = new TextBox();
  TextBox mapsLink = new TextBox();
  
  public GeoLocationGenerator(ChangeListener listener) {
    latitude.addStyleName(StylesDefs.INPUT_FIELD_REQUIRED);
    longitude.addStyleName(StylesDefs.INPUT_FIELD_REQUIRED);
    latitude.addChangeListener(listener);
    longitude.addChangeListener(listener);
    query.addChangeListener(listener);
  }
  
  public String getName() {
    return "Geo location";
  }

  public String getText() throws GeneratorException {
    String que = getQueryField();
    if (null != que && que.length() > 0) {
      if (null == getLatitudeField()) {
        latitude.setText("0");
      }
      if (null == getLongitudeField()) {
        longitude.setText("0");
      }
    }
    String lat = getLatitudeField();
    String lon = getLongitudeField();
    
    if (que.length() > 0) {
      return "geo:"+lat+","+lon+"?q="+que;
    }

    return "geo:"+lat+","+lon;
  }

  private String getQueryField() {
    String que = query.getText();
    que = que.replace("&", "%26");
    return que;
  }

  private String getLongitudeField() throws GeneratorException {
    String lon = longitude.getText();
    if (!lon.matches(LON_REGEXP)) {
      throw new GeneratorException("Longitude is not a correct value.");
    }
    double val = Double.parseDouble(lon);
    if (val < -180 || val > 180) {
      throw new GeneratorException("Longitude must be in [-180:180]");
    }
    return lon;
  }

  private String getLatitudeField() throws GeneratorException {
    String lat = latitude.getText();
    if (!lat.matches(LAT_REGEXP)) {
      throw new GeneratorException("Latitude is not a correct value.");
    }
    double val = Double.parseDouble(lat);
    if (val < -90 || val > 90) {
      throw new GeneratorException("Latitude must be in [-90:90]");
    }
    return lat;
  }

  public Grid getWidget() {
    if (table != null) {
      return table;
    }
    table = new Grid(5, 2);
    
    table.setText(0, 0, "Latitude");
    table.setWidget(0, 1, latitude);
    table.setText(1, 0, "Longitude");
    table.setWidget(1, 1, longitude);
    table.setText(2, 0, "Query");
    table.setWidget(2, 1, query);
    table.setText(3, 0, "OR");
    table.setText(3, 1, "enter a Google Maps link and click Fill:");
    // looks like this:
    // http://maps.google.com/?ie=UTF8&ll=40.741404,-74.00322&spn=0.001484,0.003101&z=18
    Button fill = new Button("Fill &uarr;");
    fill.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        fillWithMaps();
      }
    });
    table.setWidget(4, 0, fill);
    table.setWidget(4, 1, mapsLink);
    
    return table;
  }

  protected void fillWithMaps() {
    String link = mapsLink.getText();
    if (!link.matches("http://maps.google.com/.*")) {
      return;
    }
    String q = "";
    if (link.matches(".*&q=[^&]*&.*")) {
      for (int i = link.indexOf("&q=") + 3;
          i < link.length() && link.charAt(i) != '&'; ++i) {
        q += link.charAt(i);
      }
      // special cases:
      q = q.replace("+", " ");
      q = q.replace("%26", "&");
    }
    
    String lat = "";
    String lon = "";
    if (link.matches(".*&s?ll=[^&]*&.*")) {
      boolean beforeComa = true;
      int start = 0;
      if (link.indexOf("&sll=") == -1) {
        start = link.indexOf("&ll=") + 4;
      } else {
        start = link.indexOf("&sll=") + 5;
      }
      for (int i = start; i < link.length() && link.charAt(i) != '&'; ++i) {
        if (beforeComa) {
          if (link.charAt(i) == ',') {
            beforeComa = false;
          } else {
            lat += link.charAt(i); 
          }
        } else {
          lon += link.charAt(i);
        }
      }
    }
    
    query.setText(URL.decode(q));
    latitude.setText(lat);
    longitude.setText(lon);
  }

  public void validate(Widget widget) throws GeneratorException {
    if (widget == latitude) getLatitudeField();
    if (widget == longitude) getLongitudeField();
  }

  public void setFocus() {
    latitude.setFocus(true);
  }
}