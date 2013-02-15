package de.unidue.wiwi.tdr.kn3.rasp_home;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

public class DatabaseClass {

	public List<Node> nodes;
	public List<Room> rooms;
	public User user;
	public Backend backend;

	public DatabaseClass() {
		nodes = new ArrayList<Node>();
		rooms = new ArrayList<Room>();
		user = new User();
		backend = new Backend();
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
				export += "<name>" + (this.name == null ? "" : this.name) + "</name>";
			}
			if (room) {
				export += "<room>" + (this.room == null ? "" : this.room) + "</room>";
			}
			if (title) {
				export += "<title>" + (this.title == null ? "" : this.title) + "</title>";
			}
			if (type) {
				export += "<type>" + (this.type == null ? "" : this.type) + "</type>";
			}
			if (value) {
				export += "<value>" + (this.value == null ? "" : this.value) + "</value>";
			}
			export += "</node>";
			return export;
		}
		
		public static boolean UpdateList(List<Node> list, Node element) {
			for (Node node : list) {
				if (node.name.equals(element.name)) {
					if (element.room != null) {
						node.room = element.room;
					}
					if (element.title != null) {
						node.title = element.title;
					}
					if (element.type != null) {
						node.type = element.type;
					}
					if (element.value != null) {
						node.value = element.value;
					}
					return true;
				}
			}
			list.add(element);
			return true;
		}

		public static Node Get(String input) {
			XmlPullParser parser = Xml.newPullParser();
			try {
				parser.setInput(new StringReader(input));
				return Get(parser);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		private static Node Get(XmlPullParser parser) {
			Node node = new Node();
			try {
				parser.require(XmlPullParser.START_TAG, null, "node");
				while (parser.nextTag() != XmlPullParser.END_TAG) {
					if (parser.getName().equals("name")) {
						node.name = GetText(parser, "name");
					} else if (parser.getName().equals("room")) {
						node.room = GetText(parser, "room");
					} else if (parser.getName().equals("title")) {
						node.title = GetText(parser, "title");
					} else if (parser.getName().equals("type")) {
						node.type = GetText(parser, "type");
					} else if (parser.getName().equals("value")) {
						node.value = Integer.parseInt(GetText(parser, "value"));
					}
				}
				parser.require(XmlPullParser.END_TAG, null, "node");
				return node;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public static List<Node> GetList(String input) {
			List<Node> nodes = new ArrayList<Node>();
			Node node = new Node();
			XmlPullParser parser = Xml.newPullParser();
			try {
				parser.setInput(new StringReader(input));
				parser.nextTag();
				parser.require(XmlPullParser.START_TAG, null, "nodes");
				while (parser.nextTag() != XmlPullParser.END_TAG) {
					node = Get(parser);
					if (node != null) {
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
				export += "<name>" + (this.name == null ? "" : this.name) + "</name>";
			}
			export += "</room>";
			return export;
		}
		
		public static boolean UpdateList(List<Room> list, Room element) {
			for (Room user : list) {
				if (user.name.equals(element.name)) {
					return true;
				}
			}
			list.add(element);
			return true;
		}

		public static Room Get(String input) {
			XmlPullParser parser = Xml.newPullParser();
			try {
				parser.setInput(new StringReader(input));
				return Get(parser);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		private static Room Get(XmlPullParser parser) {
			Room room = new Room();
			try {
				parser.require(XmlPullParser.START_TAG, null, "room");
				while (parser.nextTag() != XmlPullParser.END_TAG) {
					if (parser.getName().equals("name")) {
						room.name = GetText(parser, "name");
					}
				}
				parser.require(XmlPullParser.END_TAG, null, "room");
				return room;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public static List<Room> GetList(String input) {
			List<Room> rooms = new ArrayList<Room>();
			XmlPullParser parser = Xml.newPullParser();
			try {
				parser.setInput(new StringReader(input));
				parser.nextTag();
				parser.require(XmlPullParser.START_TAG, null, "rooms");
				while (parser.nextTag() != XmlPullParser.END_TAG) {
					Room room = Get(parser);
					if (room != null) {
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
				export += "<name>" + (this.name == null ? "" : this.name) + "</name>";
			}
			if (login) {
				export += "<login>" + (this.login == null ? "" : this.login) + "</login>";
			}
			if (room) {
				export += "<room>" + (this.room == null ? "" : this.room) + "</room>";
			}
			if (admin) {
				export += "<admin>" + (this.admin == null ? "" : this.admin) + "</admin>";
			}
			export += "</user>";
			return export;
		}
		
		public static boolean UpdateList(List<User> list, User element) {
			for (User user : list) {
				if (user.name.equals(element.name)) {
					if (element.login != null) {
						user.login = element.login;
					}
					if (element.room != null) {
						user.room = element.room;
					}
					if (element.admin != null) {
						user.admin = element.admin;
					}
					return true;
				}
			}
			list.add(element);
			return true;
		}

		public static User Get(String input) {
			XmlPullParser parser = Xml.newPullParser();
			try {
				parser.setInput(new StringReader(input));
				return Get(parser);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		private static User Get(XmlPullParser parser) {
			User user = new User();
			try {
				parser.require(XmlPullParser.START_TAG, null, "user");
				while (parser.nextTag() != XmlPullParser.END_TAG) {
					if (parser.getName().equals("name")) {
						user.name = GetText(parser, "name");
					}
				}
				parser.require(XmlPullParser.END_TAG, null, "user");
				return user;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public static List<User> GetList(String input) {
			List<User> users = new ArrayList<User>();
			XmlPullParser parser = Xml.newPullParser();
			try {
				parser.setInput(new StringReader(input));
				parser.nextTag();
				parser.require(XmlPullParser.START_TAG, null, "rooms");
				while (parser.nextTag() != XmlPullParser.END_TAG) {
					User user = Get(parser);
					if (user != null) {
						users.add(user);
					}
				}
				parser.require(XmlPullParser.END_TAG, null, "rooms");
				return users;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	public static class Backend {
		public String ip = null;
		public String name = null;
		public String pass = null;
	}
}
