package de.unidue.wiwi.tdr.kn3.rasp_home;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Base64;
import android.util.Log;

public class CommunicationClass extends Thread {

	public Client client;
	public Server server;
	public Zeroconf zeroconf;

	public CommunicationClass(Context context) {
		client = new Client(context);
		server = new Server(context);
		zeroconf = new Zeroconf(context);
	}

	private static String GetStringOfInputStream(InputStream stream) {
		try {
			InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
			String string = "";
			int ch;
			while ((ch = reader.read()) > 0) {
				string += (char) ch;
			}
			reader.close();
			return string;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static class RequestMessage {
		public Method method;
		public Type type;
		public String name;
		public String attrib;
		public String value;
		public Value_Type value_type;

		public enum Method {
			GET, PUT, POST, DELETE, ZERO
		}

		public enum Type {
			Backend, Monitor, Node, Room, User
		}
		
		public enum Value_Type {
			text_plain, text_xml
		}

		public RequestMessage(Method method, Type type, String name, String attrib, String value, Value_Type value_type) {
			this.method = method;
			this.type = type;
			this.name = name;
			this.attrib = attrib;
			this.value = value;
			this.value_type = value_type;
		}
	}

	public static class ResponseMessage {
		public int status;
		public String reason;
		public String value;
		public Value_Type value_type;

		public enum Value_Type {
			text_plain, text_xml
		}
		
		public ResponseMessage(int status, String reason, String value, Value_Type value_type) {
			this.status = status;
			this.reason = reason;
			this.value = value;
			this.value_type = value_type;
		}
	}

	public static class Client {
		private DefaultHttpClient client;
		private String user_pass = null;
		private String ip_port = null;

		public Client(Context context) {
			try {
				KeyStore clientKS = KeyStore.getInstance(KeyStore.getDefaultType());
				clientKS.load(context.getAssets().open("client.keystore"), "rhclien".toCharArray());
				SSLSocketFactory clientSF = new SSLSocketFactory(clientKS);
				clientSF.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				Scheme clientSch = new Scheme("https", clientSF, 443);
				client = new DefaultHttpClient();
				client.getConnectionManager().getSchemeRegistry().register(clientSch);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void SetTimeout(int timeout) {
			HttpParams params = client.getParams();
			HttpConnectionParams.setConnectionTimeout(params, timeout);
			HttpConnectionParams.setSoTimeout(params, timeout);
		}

		public ResponseMessage SendRequest(RequestMessage message) {
			HttpUriRequest request = null;
			String uri = "https://" + ip_port + "/";
			switch (message.type) {
			case Backend:
				uri += "backend";
				break;
			case Monitor:
				uri += "monitor";
				break;
			case Node:
				uri += "node";
				break;
			case Room:
				uri += "room";
				break;
			case User:
				uri += "user";
				break;
			}
			uri += message.name != null ? "/" + message.name : "";
			if (message.attrib != null) {
				if (message.name != null) {
					uri += "/" + message.attrib;
				} else {
					uri += "?" + message.attrib + "=" + message.value;
				}
			}
			switch (message.method) {
			case GET:
				request = new HttpGet(uri);
				break;
			case PUT:
				request = new HttpPut(uri);
				try {
					((HttpPut) request).setEntity(new StringEntity(message.value, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				break;
			case POST:
				request = new HttpPost(uri);
				try {
					((HttpPost) request).setEntity(new StringEntity(message.value, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				break;
			case DELETE:
				request = new HttpDelete(uri);
				break;
			}
			if (message.value_type != null) {
				switch (message.value_type) {
				case text_plain:
					request.addHeader("Content-Type", "text/plain");
					break;
				case text_xml:
					request.addHeader("Content-Type", "text/xml");
					break;
				}
			}
			UsernamePasswordCredentials creds = new UsernamePasswordCredentials(user_pass);
			request.addHeader(BasicScheme.authenticate(creds, "UTF-8", false));
			try {
				HttpResponse response = client.execute(request);
				int status = response.getStatusLine().getStatusCode();
				String reason = response.getStatusLine().getReasonPhrase();
				String value = GetStringOfInputStream(response.getEntity().getContent());
				ResponseMessage.Value_Type value_type = null;
				if (response.getEntity().getContentType().getValue().equals("text/plain")) {
					value_type = ResponseMessage.Value_Type.text_plain;
				} else if (response.getEntity().getContentType().getValue().equals("text/xml")) {
					value_type = ResponseMessage.Value_Type.text_xml;
				}
				return new ResponseMessage(status, reason, value, value_type);
			} catch (Exception e) {
				return null;
			}
		}

		public void SetUserPass(String user_pass) {
			this.user_pass = user_pass;
		}

		public void SetServerIpPort(String ip_port) {
			this.ip_port = ip_port;
		}
	}

	public static class Server extends Thread {
		public Observable<RequestMessage> observer;

		private Context context;
		private HttpService service;
		private HttpRequestHandlerRegistry registry;
		private SSLServerSocketFactory serverSF;
		private SSLServerSocket serverSocket;
		private boolean run = false;
		private String user_pass = null;
		private int port = 0;

		public Server(Context context) {
			this.context = context;
			observer = new Observable<RequestMessage>();
			service = new HttpService(new BasicHttpProcessor(), new DefaultConnectionReuseStrategy(),
					new DefaultHttpResponseFactory());
			registry = new HttpRequestHandlerRegistry();
			registry.register("/node/*", new HttpRequestHandler() {
				@Override
				public void handle(HttpRequest request, HttpResponse response, HttpContext context)
						throws HttpException, IOException {
					if (AuthorizeUser(request, response)) {
						String[] requestArray = request.getRequestLine().getUri().split("/");
						RequestMessage.Method method = null;
						String name = null;
						String attrib = null;
						String value = GetStringOfInputStream(((HttpEntityEnclosingRequest) request).getEntity().getContent());
						RequestMessage.Value_Type value_type = null;
						if (request.getRequestLine().getMethod().equals("PUT")) {
							method = RequestMessage.Method.PUT;
						} else if (request.getRequestLine().getMethod().equals("POST")) {
							method = RequestMessage.Method.POST;
						} else if (request.getRequestLine().getMethod().equals("DELETE")) {
							method = RequestMessage.Method.DELETE;
						}
						if (requestArray.length >= 3) {
							name = requestArray[2];
						}
						if (requestArray.length >= 4) {
							attrib = requestArray[3];
						}
						if (((HttpEntityEnclosingRequest) request).getEntity().getContentType().getValue().equals("text/plain")) {
							value_type = RequestMessage.Value_Type.text_plain;
						} else if (((HttpEntityEnclosingRequest) request).getEntity().getContentType().getValue().equals("text/xml")) {
							value_type = RequestMessage.Value_Type.text_xml;
						}
						Server.this.observer.notifyObservers(new RequestMessage(method, RequestMessage.Type.Node, name,
								attrib, value, value_type));
					}
				}
			});
			registry.register("/room/*", new HttpRequestHandler() {
				@Override
				public void handle(HttpRequest request, HttpResponse response, HttpContext context)
						throws HttpException, IOException {
					if (AuthorizeUser(request, response)) {
						String[] requestArray = request.getRequestLine().getUri().split("/");
						RequestMessage.Method method = null;
						String name = null;
						String attrib = null;
						String value = GetStringOfInputStream(((HttpEntityEnclosingRequest) request).getEntity().getContent());
						RequestMessage.Value_Type value_type = null;
						if (request.getRequestLine().getMethod().equals("PUT")) {
							method = RequestMessage.Method.PUT;
						} else if (request.getRequestLine().getMethod().equals("POST")) {
							method = RequestMessage.Method.POST;
						} else if (request.getRequestLine().getMethod().equals("DELETE")) {
							method = RequestMessage.Method.DELETE;
						}
						if (requestArray.length >= 3) {
							name = requestArray[2];
						}
						if (requestArray.length >= 4) {
							attrib = requestArray[3];
						}
						if (((HttpEntityEnclosingRequest) request).getEntity().getContentType().getValue().equals("text/plain")) {
							value_type = RequestMessage.Value_Type.text_plain;
						} else if (((HttpEntityEnclosingRequest) request).getEntity().getContentType().getValue().equals("text/xml")) {
							value_type = RequestMessage.Value_Type.text_xml;
						}
						Server.this.observer.notifyObservers(new RequestMessage(method, RequestMessage.Type.Room, name, attrib, value, value_type));
					}
				}
			});
			service.setHandlerResolver(registry);

			try {
				SSLContext serverCon = SSLContext.getInstance("TLS");
				KeyManagerFactory serverKMF = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				KeyStore serverKS = KeyStore.getInstance(KeyStore.getDefaultType());
				serverKS.load(this.context.getAssets().open("server.keystore"), "rhclien".toCharArray());
				serverKMF.init(serverKS, "rhclien".toCharArray());
				serverCon.init(serverKMF.getKeyManagers(), null, null);
				serverSF = serverCon.getServerSocketFactory();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public boolean IsRun() {
			return run;
		}

		private boolean AuthorizeUser(HttpRequest request, HttpResponse response) {
			Header authHeader = request.getFirstHeader("Authorization");
			if (authHeader != null) {
				if (authHeader.getValue().startsWith("Basic ")) {
					String auth;
					try {
						auth = new String(Base64.decode(authHeader.getValue().substring(6), Base64.DEFAULT), "UTF-8");
						if (auth.equals(user_pass)) {
							return true;
						}
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}
			response.addHeader("WWW-Authenticate", "BASIC realm=\"Authentication\"");
			response.setStatusCode(401);
			return false;
		}

		public boolean Start(int port) {
			if (!run) {
				try {
					this.port = port;
					serverSocket = (SSLServerSocket) serverSF.createServerSocket(this.port);
					serverSocket.setReuseAddress(true);
					run = true;
					super.start();
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			} else {
				return false;
			}
		}

		public boolean Stop() {
			if (run) {
				try {
					serverSocket.close();
					run = false;
					return true;
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			} else {
				return false;
			}
		}

		@Override
		public void run() {
			super.run();
			while (run) {
				try {
					Socket socket = serverSocket.accept();
					DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
					conn.bind(socket, new BasicHttpParams());
					service.handleRequest(conn, new BasicHttpContext());
					conn.shutdown();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void SetAuthorizedUserPass(String user_pass) {
			this.user_pass = user_pass;
		}
	}

	public static class Zeroconf extends Thread {
		public Observable<RequestMessage> observer;

		private Context context;
		private DatagramSocket socket;
		private boolean run = false;
		private int port = 0;

		public Zeroconf(Context context) {
			this.context = context;
			observer = new Observable<RequestMessage>();
		}

		public boolean IsRun() {
			return run;
		}

		private InetAddress GetBroadcastAddress() throws IOException {
			WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			DhcpInfo dhcp = wifi.getDhcpInfo();

			int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
			byte[] quads = new byte[4];
			for (int k = 0; k < 4; k++)
				quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
			return InetAddress.getByAddress(quads);
		}

		public boolean Start(int port) {
			if (!run) {
				try {
					this.port = port;
					socket = new DatagramSocket(this.port);
					run = true;
					super.start();
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			} else {
				return false;
			}
		}

		public boolean Stop() {
			if (run) {
				try {
					socket.close();
					run = false;
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			} else {
				return false;
			}
		}

		@Override
		public void run() {
			super.run();
			try {
				byte[] data = "hello client".getBytes("UTF-8");
				DatagramPacket packet = new DatagramPacket(data, data.length, GetBroadcastAddress(), port);

				Log.d(MainApplication.RH_TAG, "Send hello: " + packet.getAddress().toString() + ":" + packet.getPort());

				socket.send(packet);
				socket.receive(packet);

				while (run) {
					byte[] buf = new byte[13];
					packet = new DatagramPacket(buf, buf.length);
					socket.receive(packet);

					if ((new String(packet.getData(), "UTF-8")).equals("hello backend")) {
						observer.notifyObservers(new RequestMessage(RequestMessage.Method.ZERO,
								RequestMessage.Type.Backend, null, null, packet.getAddress().toString() + ":"
										+ packet.getPort(), null));

						Log.d(MainApplication.RH_TAG, "Hello received: " + packet.getAddress().toString() + ":"
								+ packet.getPort());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
