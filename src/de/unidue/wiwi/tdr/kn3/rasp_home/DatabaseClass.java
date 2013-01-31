package de.unidue.wiwi.tdr.kn3.rasp_home;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

public class DatabaseClass {
	
	public List<Node> nodes;
	public List<Room> rooms;
	public User user;
	public String backend_ip;
	public String backend_pass;
	
	public DatabaseClass() {
		nodes = new ArrayList<Node>();
		rooms = new ArrayList<Room>();
		user = new User();
	}

	private static String GetText(XmlPullParser parser, String name) {
		String text = null;
		try {
			parser.require(XmlPullParser.START_TAG, null, name);
			if (parser.next() == XmlPullParser.TEXT) {
				text = parser.getText();
				parser.nextTag();
			} else {
				text = "";
			}
			parser.require(XmlPullParser.END_TAG, null, name);
			return text;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static class Node {
		public String name = null;
		public String room = null;
		public String title = null;
		public String type = null;
		public Integer value = null;

		public String Export(boolean name, boolean room, boolean title, boolean type, boolean value) {
			String export = "<node>";
			if (name) {
				export +="<name>" + (this.name == null ? "" : this.name) + "</name>";
			}
			if (room) {
				export +="<room>" + (this.room == null ? "" : this.room) + "</room>";
			}
			if (title) {
				export +="<title>" + (this.title == null ? "" : this.title) + "</title>";
			}
			if (type) {
				export +="<type>" + (this.type == null ? "" : this.type) + "</type>";
			}
			if (value) {
				export +="<value>" + (this.value == null ? "" : this.value) + "</value>";
			}
			export += "</node>";			
			return export;
		}

		public boolean Import(InputStream stream) {
			XmlPullParser parser = Xml.newPullParser();
			try {
				parser.setInput(stream, "UTF-8");
				return Import(parser);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		private boolean Import(XmlPullParser parser) {
			try {
				parser.require(XmlPullParser.START_TAG, null, "node");
				while (parser.nextTag() != XmlPullParser.END_TAG) {
					if (parser.getName().equals("name")) {
						name = GetText(parser, "name");
					} else if (parser.getName().equals("room")) {
						room = GetText(parser, "room");
					} else if (parser.getName().equals("title")) {
						title = GetText(parser, "title");
					} else if (parser.getName().equals("type")) {
						type = GetText(parser, "type");
					} else if (parser.getName().equals("value")) {
						value = Integer.parseInt(GetText(parser, "value"));
					}
				}
				parser.require(XmlPullParser.END_TAG, null, "node");
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		public static List<Node> GetNodes(InputStream stream) {
			List<Node> nodes = new ArrayList<Node>();
			XmlPullParser parser = Xml.newPullParser();
			try {
				parser.setInput(stream, "UTF-8");
				parser.nextTag();
				parser.require(XmlPullParser.START_TAG, null, "nodes");
				while (parser.nextTag() != XmlPullParser.END_TAG) {
					Node node = new Node();
					if (node.Import(parser)) {
						nodes.add(node);
					}
				}
				parser.require(XmlPullParser.END_TAG, null, "nodes");
				return nodes;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public static class Room {
		public String name = null;

		public String Export(boolean name) {
			String export = "<room>";
			if (name) {
				export +="<name>" + (this.name == null ? "" : this.name) + "</name>";
			}
			export += "</room>";
			return export;
		}

		public boolean Import(InputStream stream) {
			XmlPullParser parser = Xml.newPullParser();
			try {
				parser.setInput(stream, "UTF-8");
				return Import(parser);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		private boolean Import(XmlPullParser parser) {
			try {
				parser.require(XmlPullParser.START_TAG, null, "room");
				while (parser.nextTag() != XmlPullParser.END_TAG) {
					if (parser.getName().equals("name")) {
						name = GetText(parser, "name");
					}
				}
				parser.require(XmlPullParser.END_TAG, null, "room");
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		public static List<Room> GetRooms(InputStream stream) {
			List<Room> rooms = new ArrayList<Room>();
			XmlPullParser parser = Xml.newPullParser();
			try {
				parser.setInput(stream, "UTF-8");
				parser.nextTag();
				parser.require(XmlPullParser.START_TAG, null, "rooms");
				while (parser.nextTag() != XmlPullParser.END_TAG) {
					Room room = new Room();
					if (room.Import(parser)) {
						rooms.add(room);
					}
				}
				parser.require(XmlPullParser.END_TAG, null, "rooms");
				return rooms;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public static class User {
		public String name = null;
		public Boolean login = null;
		public String room = null;
		public Boolean admin = null;

		public String Export(boolean name, boolean login, boolean room, boolean admin) {
			String export = "<user>";
			if (name) {
				export +="<name>" + (this.name == null ? "" : this.name) + "</name>";
			}
			if (login) {
				export +="<login>" + (this.login == null ? "" : this.login) + "</login>";
			}
			if (room) {
				export +="<room>" + (this.room == null ? "" : this.room) + "</room>";
			}
			if (admin) {
				export +="<admin>" + (this.admin == null ? "" : this.admin) + "</admin>";
			}
			export += "</user>";
			return export;
		}
	}
}
