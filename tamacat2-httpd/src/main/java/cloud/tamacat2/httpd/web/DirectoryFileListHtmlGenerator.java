/*
 * Copyright 2024 tamacat.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cloud.tamacat2.httpd.web;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import cloud.tamacat2.httpd.util.DateUtils;

public class DirectoryFileListHtmlGenerator {

	String timeZone = "UTC";
	Locale locale = Locale.getDefault();
	String dateFormat = "yyyy-MM-dd HH:mm z";
						//"yyyy-MM-dd'T'HH:mm:ss'Z'";
	
	String htmlStart = 
			  "<!DOCTYPE html>\r\n"
			+ "<html>\r\n"
			+ "  <head>\r\n"
			+ "    <meta charset=\"UTF-8\" />\r\n"
			+ "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\" />\r\n"
			+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\r\n"
			+ "    <title>File List</title>\r\n"
			+ "    <style type=\"text/css\">\r\n"
			+ "      html {font-size:100%;}\r\n"
			+ "      body {font-size:78%;\r\n"
			+ "            font-family:Verdana, Arial, Sans-Serif;\r\n"
			+ "            background:#fff; color:#333; \r\n"
			+ "            margin:0; padding:0;}\r\n"
			+ "      h1{font-size:110%; color:white; background-color:#444;padding:5px; margin:0;}\r\n"
			+ "      h3{font-size:100%; color:white; background-color:#444;padding:2px;}\r\n"
			+ "      a{color:#000066; text-decoration:none;} hr{color : #666;}\r\n"
			+ "      table{margin: 10px; padding: 4px;}\r\n"
			+ "      th{text-align:left; border-bottom:1px solid #666;}\r\n"
			+ "      tr.row:hover{background-color:#eee;}\r\n"
			+ "      td {padding:1px 8px; border-bottom:1px solid #f9f9f9;}\r\n"
			+ "      #search {float:right; margin-top:5px; }\r\n"
			+ "      input[type=submit] {background:#eee; border:1px solid transparent; padding:3px 8px;}\r\n"
			+ "      input[type=submit]:hover {background:#ddd;}\r\n"
			+ "    </style>\r\n"
			+ "  </head>\r\n"
			+ "  <body>\r\n";
	
	String htmlEnd = 
			  "  </body>\r\n"
			+ "</html>\r\n";
	
	String tableStart = 
			  "    <table style=\"width:95%;\">\r\n"
			+ "      <tr>\r\n"
			+ "        <th style=\"width:65%;\">Name</th>\r\n"
			+ "        <th style=\"width:20%;\" nowrap=\"nowrap\">Last modified</th>\r\n"
			+ "        <th style=\"witdh:15%;text-align:right;\">Size</th>\r\n"
			+ "      </tr>\r\n";
	
	String tableEnd = 
			  "    </table>\r\n";
	
	String parentLinkRow =
			  "      <tr>\r\n"
			+ "        <td colspan=\"3\"><a href=\"../\"><span>../</span></a></td>\r\n"
			+ "      </tr>\r\n";
	
	public String html(final Collection<File> files) {
		StringBuilder html = new StringBuilder()
				.append(htmlStart)
				.append(tableStart)
				.append(parentLinkRow);
		
		for (File file : files) {
			String time = DateUtils.getTime(
				new Date(file.lastModified()), dateFormat,
				locale,
				TimeZone.getTimeZone(timeZone)
			);

			String name = file.getName();
			
			if (file.isDirectory()) {
				html.append(
				"      <tr class=\"row\">\r\n"
			  + "        <td><a href=\""+getPath(name)+"/\"><span>"+name+"/</span></a></td>\r\n"
			  + "        <td nowrap=\"nowrap\">"+time+"</td>\r\n"
			  + "        <td style=\"text-align:right;\" nowrap=\"nowrap\">-</td>\r\n"
			  + "      </tr>\r\n");
			} else {
				String size = String.format("%1$,3d KB", (long)Math.ceil(file.length()/1024d)).trim();
				html.append( 
				"      <tr class=\"row\">\r\n"
			  + "        <td><a href=\""+getPath(name)+"\"><span>"+name+"</span></a></td>\r\n"
			  + "        <td nowrap=\"nowrap\">"+time+"</td>\r\n"
			  + "        <td style=\"text-align:right;\" nowrap=\"nowrap\">"+size+"</td>\r\n"
			  + "      </tr>\r\n");
			}
		}
		html.append(tableEnd)
			.append(htmlEnd);
		return html.toString();
	}
	
	protected String getPath(final String path) {
		return URLEncoder.encode(path, StandardCharsets.UTF_8);
	}
	
	public static String encode(final String str, final Charset encoding) {
		if (str == null || str.length() == 0) {
			return str;
		} else {
			return new String(str.getBytes(encoding), StandardCharsets.ISO_8859_1);
		}
	}
}
