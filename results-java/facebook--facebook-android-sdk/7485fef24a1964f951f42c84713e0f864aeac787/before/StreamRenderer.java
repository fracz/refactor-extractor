/*
 * Copyright 2010 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.stream;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Contains logic for rendering the stream.
 *
 * @author yariv
 */
class StreamRenderer {

	private StringBuilder sb;

	public static String render(JSONObject data) {
		StreamRenderer renderer = new StreamRenderer();
		return renderer.doRender(data);
	}

	public StreamRenderer() {
		this.sb = new StringBuilder();
	}

	private String doRender(JSONObject data) {

		try {
			JSONArray posts = data.getJSONArray("data");
			String[] chunks = {
				"<html><head>",
				"<link rel=\"stylesheet\" href=\"file:///android_asset/stream.css\" type=\"text/css\">",
				"<script src=\"file:///android_asset/stream.js\"></script>",
				"</head>",
				"<body>",
				"<div id=\"header\">"
			};
			append(chunks);
			renderLink("app://logout", "logout");

			for (int i = 0; i < posts.length(); i++) {
				renderPost(posts.getJSONObject(i));
			}
			append("</body></html>");
			return getResult();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}

	public String getResult() {
		return sb.toString();
	}

	private void renderPost(JSONObject post) throws JSONException {
		append("<div class=\"post\">");
		renderFrom(post);
		renderTo(post);
		renderMessage(post);
		renderAttachment(post);
		renderActionLinks(post);
		renderLikes(post);
		renderComments(post);
		renderCommentBox(post);
		append("</div>");
	}


	private void renderFrom(JSONObject post) throws JSONException {
		JSONObject from = post.getJSONObject("from");
		String fromName = from.getString("name");
		String fromId = from.getString("id");
		renderAuthor(fromId, fromName);
	}

	private void renderTo(JSONObject post) throws JSONException {
		JSONObject to = post.optJSONObject("to");
		if (to != null) {
			JSONObject toData = to.getJSONArray("data").getJSONObject(0);
			String toName = toData.getString("name");
			String toId = toData.getString("id");
			append(" > ");
			renderProfileLink(toId, toName);
		}
	}

	private void renderProfileLink(String id, String name) {
		renderLink("fb://" + id, name);
	}

	private void renderAuthor(String id, String name) {
		String[] chunks = {
		"<div class=\"profile_pic_container\">",
		"<a href=\"fb://", id,
		"\"><img class=\"profile_pic\" src=\"http://graph.facebook.com/",
		id, "/picture\"/></a>",
	    "</div>"
		};
		append(chunks);
		renderProfileLink(id, name);
	}

	private void renderMessage(JSONObject post) {
		String message = post.optString("message");
		String[] chunks = {
				"&nbsp;<span class=\"msg\">", message, "</span>",
				"<div class=\"clear\"></div>"};
		append(chunks);
	}

	private void renderAttachment(JSONObject post) {
		String name = post.optString("name");
		String link = post.optString("link");
		String picture = post.optString("picture");
		String source = post.optString("source"); // for videos
		String caption = post.optString("caption");
		String description = post.optString("description");

		String[] fields = new String[] {
				name, link, picture, source, caption, description
		};
		boolean hasAttachment = false;
		for (String field : fields) {
			if (field != "") {
				hasAttachment = true;
				break;
			}
		}

		if (!hasAttachment) {
			return;
		}

		append("<div class=\"attachment\">");
		if (name != "") {
			append("<div class=\"title\">");
			if (link != null) {
				renderLink(link, name);
			} else {
				append(name);
			}
			append("</div>");
		}
		if (caption != "") {
			append("<div class=\"caption\">" + caption + "</div>");
		}

		if (picture != "") {
			append("<div class=\"picture\">");
			String img = "<img src=\"" + picture + "\"/>";
			if (link != "") {
				renderLink(link, img);
			} else {
				append(img);
			}
			append("</div>");
		}

		if (description != "") {
			append("<div class=\"description\">" + description + "</div>");
		}
		append("<div class=\"clear\"></div></div>");
	}

	private void renderLink(String href, String text) {
		append(new String[] {
				"<a href=\"",
				href,
				"\">",
				text,
				"</a>"
		});
	}

	private void renderActionLinks(JSONObject post) {
		HashSet<String> actions = getActions(post);
		append("<div class=\"action_links\">");
		append("<div class=\"action_link\">");
		renderTimeStamp(post);
		append("</div>");
		String post_id = post.optString("id");
		if (actions.contains("Comment")) {
			renderActionLink(post_id, "Comment", "comment");
		}
		boolean canLike = actions.contains("Like");
		renderActionLink(post_id, "Like", "like", canLike);
		renderActionLink(post_id, "Unlike", "unlike", !canLike);

		append("<div class=\"clear\"></div></div>");
	}

	private void renderActionLink(String post_id, String title, String func) {
		renderActionLink(post_id, title, func, true);
	}

	private void renderActionLink(String post_id, String title, String func, boolean visible) {
		String extraClass = visible ? "" : "hidden";
		String[] chunks = new String[] {
				"<div id=\"", func, post_id, "\" class=\"action_link ", extraClass, "\">",
				"<a href=\"#\" onclick=\"",func, "('",	post_id, "'); return false;\">",
				title,
				"</a></div>"
			};
		append(chunks);
	}

	private void renderTimeStamp(JSONObject post) {
		String dateStr = post.optString("created_time");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
		ParsePosition pos = new ParsePosition(0);
		long then  = formatter.parse(dateStr, pos).getTime();
		long now = new Date().getTime();

		long seconds = (now - then)/1000;
		long minutes = seconds/60;
		long hours = minutes/60;
		long days = hours/24;

		String friendly = null;
		long num = 0;
		if (days > 0) {
			num = days;
			friendly = days + " day";
		} else if (hours > 0) {
			num = hours;
			friendly = hours + " hour";
		} else if (minutes > 0) {
			num = minutes;
			friendly = minutes + " minute";
		} else {
			num = seconds;
			friendly = seconds + " second";
		}
		if (num > 1) {
			friendly += "s";
		}
		String[] chunks = new String[] {
			"<div class=\"timestamp\">",
			friendly,
			" ago",
			"</div>"
		};
		append(chunks);
	}

	private HashSet<String> getActions(JSONObject post) {
		HashSet<String> actionsSet = new HashSet<String>();
		JSONArray actions = post.optJSONArray("actions");
		if (actions != null) {
			for (int j = 0; j < actions.length(); j++) {
				JSONObject action = actions.optJSONObject(j);
				String actionName = action.optString("name");
				actionsSet.add(actionName);
			}
		}
		return actionsSet;
	}

	private void renderLikes(JSONObject post) {
		int numLikes = post.optInt("likes", 0);
		if (numLikes > 0) {
			String desc = numLikes == 1 ?
						"person likes this" :
						"people like this";
			String[] chunks = new String[] {
				"<div class=\"like_icon\">",
				"<img src=\"file:///android_asset/like_icon.png\"/>",
				"</div>",
				"<div class=\"num_likes\">",
				new Integer(numLikes).toString(),
				" ",
				desc,
				"</div>"
			};
			append(chunks);
		}
	}

	private void renderComments(JSONObject post) throws JSONException {
		append("<div class=\"comments\" id=\"comments" + post.optString("id") + "\">");
		JSONObject comments = post.optJSONObject("comments");
		if (comments != null) {
			JSONArray data = comments.optJSONArray("data");
			for (int j = 0; j < data.length(); j++) {
				JSONObject comment = data.getJSONObject(j);
				renderComment(comment);
			}
		}
		append("</div>");
	}

	public static String renderSingleComment(JSONObject comment) {
		StreamRenderer renderer = new StreamRenderer();
		renderer.renderComment(comment);
		return renderer.getResult();
	}

	private void renderComment(JSONObject comment) {
		JSONObject from = comment.optJSONObject("from");
		String authorName = from.optString("name");
		String authorId = from.optString("id");
		String message = comment.optString("message");
		append("<div class=\"comment\">");
		renderAuthor(authorId, authorName);
		String[] chunks = {
				"&nbsp;",
				  message,
				  "</div>"
				  };
		append(chunks);
	}


	private void renderCommentBox(JSONObject post) {
		String id = post.optString("id");
		String[] chunks = new String[] {
				"<div class=\"comment_box\" id=\"comment_box", id, "\">",
				"<input id=\"comment_box_input", id, "\"/>",
				"<button onclick=\"postComment('", id , "');\">Post</button>",
				"<div class=\"clear\"></div>",
				"</div>"
		};
		append(chunks);
	}


	private void append(String str) {
		sb.append(str);
	}
	private void append(String[] chunks) {
		for (String chunk : chunks) {
			sb.append(chunk);
		}
	}
}