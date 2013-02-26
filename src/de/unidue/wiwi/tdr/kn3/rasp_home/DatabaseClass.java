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
		public String input = null;
		public String output = null;

		public static String ExportOne(Node element, List<String> attribs) {
			String export = "<node>";
			if (attribs.contains("name")) {
				export += "<name>" + element.name + "</name>";
			}
			if (attribs.contains("room")) {
				export += "<room>" + element.room + "</room>";
			}
			if (attribs.contains("title")) {
				export += "<title>" + element.title + "</title>";
			}
			if (attribs.contains("type")) {
				export += "<type>" + element.type + "</type>";
			}
			if (attribs.contains("input")) {
				export += "<input>" + element.input + "</input>";
			}
			if (attribs.contains("output")) {
				export += "<output>" + element.output + "</output>";
			}
			export += "</node>";
			return export;
		}
		
		public static String ExportAll(List<Node> elements, List<String> attribs) {
			String export = "<nodes>";
			for (Node element : elements) {
				export += ExportOne(element, attribs);
			}
			export += "</nodes>";
			return export;
		}

		public static Node GetOne(List<Node> elements, String name) {
			for (Node element : elements) {
				if (element.name.equals(name)) {
					return element;
				}
			}
			return null;
		}
		
		public static List<Node> GetAll(List<Node> elements, String room) {
			if (room == null) {
				return elements;
			} else {
				List<Node> roomElements = new ArrayList<Node>();
				for (Node element : elements) {
					if (element.room.equals(room)) {
						roomElements.add(element);
					}
				}
				return roomElements;
			}
		}

		public static Node AddOne(List<Node> elements, Node new_element) {
			Node element = GetOne(elements, new_element.name);
			if (element == null) {
				elements.add(new_element);
				return new_element;
			} else {
				return null;
			}
		}
		
		public static void AddAll(List<Node> elements, List<Node> new_elements) {
			elements.addAll(new_elements);
		}

		public static Node DelOne(List<Node> elements, Node element) {
			element = GetOne(elements, element.name);
			if (element != null) {
				elements.remove(element);
				return element;
			} else {
				return null;
			}
		}
		
		public static void DelAll(List<Node> elements) {
			elements.clear();
		}

		public static Node EditOne(Node element, String attrib, String value) {
			if (attrib.equals("name")) {
				element.name = value;
			} else if (attrib.equals("room")) {
				element.room = value;
			} else if (attrib.equals("title")) {
				element.title = value;
			} else if (attrib.equals("type")) {
				element.type = value;
			} else if (attrib.equals("input")) {
				element.input = value;
			} else if (attrib.equals("output")) {
				element.output = value;
			} else {
				return null;
			}
			return element;
		}

		public static Node ImportOne(String input, Node element, String name) {
			if (element == null) {
				element = new Node();
			}
			XmlPullParser parser = Xml.newPullParser();
			try {
				parser.setInput(new StringReader(input));
				parser.require(XmlPullParser.START_TAG, null, "node");
				while (parser.nextTag() != XmlPullParser.END_TAG) {
					if (parser.getName().equals("name")) {
						if (name == null) {
							name = GetText(parser, "name");
						}
						if (name != null) {
							EditOne(element, "name", name);
						}
					} else if (parser.getName().equals("room")) {
						String room = GetText(parser, "room");
						if (room != null) {
							EditOne(element, "room", room);
						}
					} else if (parser.getName().equals("title")) {
						String title = GetText(parser, "title");
						if (title != null) {
							EditOne(element, "title", title);
						}
					} else if (parser.getName().equals("type")) {
						String type = GetText(parser, "type");
						if (type != null) {
							EditOne(element, "type", type);
						}
					} else if (parser.getName().equals("input")) {
						input = GetText(parser, "input");
						if (input != null) {
							EditOne(element, "input", input);
						}
					} else if (parser.getName().equals("output")) {
						String output = GetText(parser, "output");
						if (output != null) {
							EditOne(element, "output", output);
						}
					}
				}
				parser.require(XmlPullParser.END_TAG, null, "node");
				return element;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public static List<Node> ImportAll(String input) {
			List<Node> elements = new ArrayList<Node>();
			Node element;
			XmlPullParser parser = Xml.newPullParser();
			try {
				parser.setInput(new StringReader(input));
				parser.nextTag();
				parser.require(XmlPullParser.START_TAG, null, "nodes");
				while (parser.nextTag() != XmlPullParser.END_TAG) {
					element = ImportOne(parser.getText(), null, null);
					if (element != null) {
						elements.add(element);
					}
				}
				parser.require(XmlPullParser.END_TAG, null, "nodes");
				return elements;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public static class Room {
		public String name = null;

		public static String ExportOne(Room element, List<String> attribs) {
			String export = "<room>";
			if (attribs.contains("name")) {
				export += "<name>" + element.name + "</name>";
			}
			export += "</room>";
			return export;
		}
		
		public static String ExportAll(List<Room> elements, List<String> attribs) {
			String export = "<rooms>";
			for (Room element : elements) {
				export += ExportOne(element, attribs);
			}
			export += "</rooms>";
			return export;
		}

		public static Room GetOne(List<Room> elements, String name) {
			for (Room element : elements) {
				if (element.name.equals(name)) {
					return element;
				}
			}
			return null;
		}
		
		public static List<Room> GetAll(List<Room> elements) {
			return elements;
		}

		public static Room AddOne(List<Room> elements, Room new_element) {
			Room element = GetOne(elements, new_element.name);
			if (element == null) {
				elements.add(new_element);
				return new_element;
			} else {
				return null;
			}
		}
		
		public static void AddAll(List<Room> elements, List<Room> new_elements) {
			elements.addAll(new_elements);
		}

		public static Room DelOne(List<Room> elements, Room element) {
			element = GetOne(elements, element.name);
			if (element != null) {
				elements.remove(element);
				return element;
			} else {
				return null;
			}
		}
		
		public static void DelAll(List<Room> elements) {
			elements.clear();
		}

		public static Room EditOne(Room element, String attrib, String value) {
			if (attrib.equals("name")) {
				element.name = value;
			} else {
				return null;
			}
			return element;
		}

		public static Room ImportOne(String input, Room element, String name) {
			if (element == null) {
				element = new Room();
			}
			XmlPullParser parser = Xml.newPullParser();
			try {
				parser.setInput(new StringReader(input));
				parser.require(XmlPullParser.START_TAG, null, "room");
				while (parser.nextTag() != XmlPullParser.END_TAG) {
					if (parser.getName().equals("name")) {
						if (name == null) {
							name = GetText(parser, "name");
						}
						if (name != null) {
							EditOne(element, "name", name);
						}
					}
				}
				parser.require(XmlPullParser.END_TAG, null, "room");
				return element;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public static List<Room> ImportAll(String input) {
			List<Room> elements = new ArrayList<Room>();
			Room element;
			XmlPullParser parser = Xml.newPullParser();
			try {
				parser.setInput(new StringReader(input));
				parser.nextTag();
				parser.require(XmlPullParser.START_TAG, null, "rooms");
				while (parser.nextTag() != XmlPullParser.END_TAG) {
					element = ImportOne(parser.getText(), null, null);
					if (element != null) {
						elements.add(element);
					}
				}
				parser.require(XmlPullParser.END_TAG, null, "rooms");
				return elements;
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
		public String receive_room = null;
		public Boolean admin = null;

		public static String ExportOne(User element, List<String> attribs) {
			String export = "<user>";
			if (attribs.contains("name")) {
				export += "<name>" + element.name + "</name>";
			}
			if (attribs.contains("login")) {
				String login = null;
				if (element.login != null) {
					if (element.login == true) {
						login = "True;";
					} else {
						login = "False";
					}
				}
				export += "<login>" + login + "</login>";
			}
			if (attribs.contains("room")) {
				export += "<room>" + element.room + "</room>";
			}
			if (attribs.contains("receive_room")) {
				export += "<receive_room>" + element.receive_room + "</receive_room>";
			}
			if (attribs.contains("admin")) {
				String admin = null;
				if (element.admin != null) {
					if (element.admin == true) {
						admin = "True;";
					} else {
						admin = "False";
					}
				}
				export += "<admin>" + admin + "</admin>";
			}
			export += "</user>";
			return export;
		}
		
		public static String ExportAll(List<User> elements, List<String> attribs) {
			String export = "<users>";
			for (User element : elements) {
				export += ExportOne(element, attribs);
			}
			export += "</users>";
			return export;
		}

		public static User GetOne(List<User> elements, String name) {
			for (User element : elements) {
				if (element.name.equals(name)) {
					return element;
				}
			}
			return null;
		}
		
		public static List<User> GetAll(List<User> elements, String room) {
			if (room == null) {
				return elements;
			} else {
				List<User> roomElements = new ArrayList<User>();
				for (User element : elements) {
					if (element.room.equals(room)) {
						roomElements.add(element);
					}
				}
				return roomElements;
			}
		}

		public static User AddOne(List<User> elements, User new_element) {
			User element = GetOne(elements, new_element.name);
			if (element == null) {
				elements.add(new_element);
				return new_element;
			} else {
				return null;
			}
		}
		
		public static void AddAll(List<User> elements, List<User> new_elements) {
			elements.addAll(new_elements);
		}

		public static User DelOne(List<User> elements, User element) {
			element = GetOne(elements, element.name);
			if (element != null) {
				elements.remove(element);
				return element;
			} else {
				return null;
			}
		}
		
		public static void DelAll(List<User> elements) {
			elements.clear();
		}

		public static User EditOne(User element, String attrib, String value) {
			if (attrib.equals("name")) {
				element.name = value;
			} else if (attrib.equals("login")) {
				if (value.equals("True")) {
					element.login = true;
				} else {
					element.login = false;
				}
			} else if (attrib.equals("room")) {
				element.room = value;
			} else if (attrib.equals("receive_room")) {
				element.receive_room = value;
			} else if (attrib.equals("admin")) {
				if (value.equals("True")) {
					element.admin = true;
				} else {
					element.admin = false;
				}
			} else {
				return null;
			}
			return element;
		}

		public static User ImportOne(String input, User element, String name) {
			if (element == null) {
				element = new User();
			}
			XmlPullParser parser = Xml.newPullParser();
			try {
				parser.setInput(new StringReader(input));
				parser.require(XmlPullParser.START_TAG, null, "user");
				while (parser.nextTag() != XmlPullParser.END_TAG) {
					if (parser.getName().equals("name")) {
						if (name == null) {
							name = GetText(parser, "name");
						}
						if (name != null) {
							EditOne(element, "name", name);
						}
					} else if (parser.getName().equals("room")) {
						String room = GetText(parser, "room");
						if (room != null) {
							EditOne(element, "room", room);
						}
					} else if (parser.getName().equals("title")) {
						String title = GetText(parser, "title");
						if (title != null) {
							EditOne(element, "title", title);
						}
					} else if (parser.getName().equals("type")) {
						String type = GetText(parser, "type");
						if (type != null) {
							EditOne(element, "type", type);
						}
					} else if (parser.getName().equals("input")) {
						input = GetText(parser, "input");
						if (input != null) {
							EditOne(element, "input", input);
						}
					} else if (parser.getName().equals("output")) {
						String output = GetText(parser, "output");
						if (output != null) {
							EditOne(element, "output", output);
						}
					}
				}
				parser.require(XmlPullParser.END_TAG, null, "user");
				return element;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public static List<User> ImportAll(String input) {
			List<User> elements = new ArrayList<User>();
			User element;
			XmlPullParser parser = Xml.newPullParser();
			try {
				parser.setInput(new StringReader(input));
				parser.nextTag();
				parser.require(XmlPullParser.START_TAG, null, "users");
				while (parser.nextTag() != XmlPullParser.END_TAG) {
					element = ImportOne(parser.getText(), null, null);
					if (element != null) {
						elements.add(element);
					}
				}
				parser.require(XmlPullParser.END_TAG, null, "users");
				return elements;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public static class Backend {
		public String ip_port = null;
		public String name_pass = null;
	}
}
