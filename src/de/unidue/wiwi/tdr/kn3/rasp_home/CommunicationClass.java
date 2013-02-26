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
import de.unidue.wiwi.tdr.kn3.rasp_home.CommunicationClass.Message.Type;

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

	public static class Message {
		public static enum Type {
			zeroconfig, updatenodevalue, updatenoderoomandtitle, addroom, deleteroom, addnode, deletenode
		}

		public Type type;
		public String title;
		public String content;

		public Message(Type type, String title, String content) {
			this.type = type;
			this.title = title;
			this.content = content;
		}
	}

	public static class Client {
		private DefaultHttpClient client;
		private String user = null, pass = null;
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

		public boolean SendRequestGet(String uri) {
			HttpGet request = new HttpGet("https://" + ip_port + uri);
			return SendRequest(request);
		}

		public boolean SendRequestDelete(String uri) {
			HttpDelete request = new HttpDelete("https://" + ip_port + uri);
			return SendRequest(request);
		}

		public boolean SendRequestPost(String uri, String content) {
			try {
				HttpPost request = new HttpPost("https://" + ip_port + uri);
				request.setEntity(new StringEntity(content, "UTF-8"));
				return SendRequest(request);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		private boolean SendRequest(HttpUriRequest request) {
			try {
				request.addHeader("Content-Type", "text/plain");
				UsernamePasswordCredentials creds = new UsernamePasswordCredentials(user, pass);
				request.addHeader(BasicScheme.authenticate(creds, "UTF-8", false));
				HttpResponse response = client.execute(request);
				if (response.getStatusLine().getStatusCode() == 200) {
					Header contentHeader = response.getFirstHeader("Content-Type");
					if (contentHeader != null) {
						if (contentHeader.getValue().equals("text/plain")) {
							String responseString = GetStringOfInputStream(response.getEntity().getContent());
							Log.d(MainApplication.RH_TAG, "Response " + responseString);
						}
					}
					return true;
				} else {
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		public void setUserPass(String user, String pass) {
			this.user = user;
			this.pass = pass;
		}

		public void setServerIpPort(String ip_port) {
			this.ip_port = ip_port;
		}
	}

	public static class Server extends Thread {
		public Observable<Message> observer;

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
			observer = new Observable<Message>();
			service = new HttpService(new BasicHttpProcessor(), new DefaultConnectionReuseStrategy(),
					new DefaultHttpResponseFactory());
			registry = new HttpRequestHandlerRegistry();
			registry.register("/nodes*", new HttpRequestHandler() {
				@Override
				public void handle(HttpRequest request, HttpResponse response, HttpContext context)
						throws HttpException, IOException {
					if (AuthorizeUser(request, response)) {
						String[] requestArray = request.getRequestLine().getUri().split("/");
						if (request.getRequestLine().getMethod().equals("PUT")) {
							if (requestArray.length == 2) {
								Server.this.observer.notifyObservers(new Message(Type.addnode, request.getRequestLine()
										.getUri(), GetStringOfInputStream(((HttpEntityEnclosingRequest) request)
										.getEntity().getContent())));
							} else if (requestArray.length == 3) {
								Server.this.observer.notifyObservers(new Message(Type.updatenoderoomandtitle, request
										.getRequestLine().getUri(),
										GetStringOfInputStream(((HttpEntityEnclosingRequest) request).getEntity()
												.getContent())));
							} else if (requestArray.length == 4) {
								Server.this.observer.notifyObservers(new Message(Type.updatenodevalue, request
										.getRequestLine().getUri(),
										GetStringOfInputStream(((HttpEntityEnclosingRequest) request).getEntity()
												.getContent())));
							}
						} else if (request.getRequestLine().getMethod().equals("DELETE")) {
							if (requestArray.length == 3) {
								Server.this.observer.notifyObservers(new Message(Type.deletenode, request
										.getRequestLine().getUri(),
										GetStringOfInputStream(((HttpEntityEnclosingRequest) request).getEntity()
												.getContent())));
							}
						}
					}
				}
			});
			registry.register("/rooms*", new HttpRequestHandler() {
				@Override
				public void handle(HttpRequest request, HttpResponse response, HttpContext context)
						throws HttpException, IOException {
					if (AuthorizeUser(request, response)) {
						String[] requestArray = request.getRequestLine().getUri().split("/");
						if (request.getRequestLine().getMethod().equals("POST")) {
							if (requestArray.length == 2) {
								Server.this.observer.notifyObservers(new Message(Type.addroom, request.getRequestLine()
										.getUri(), GetStringOfInputStream(((HttpEntityEnclosingRequest) request)
										.getEntity().getContent())));
							}
						} else if (request.getRequestLine().getMethod().equals("DELETE")) {
							if (requestArray.length == 3) {
								Server.this.observer.notifyObservers(new Message(Type.deleteroom, request
										.getRequestLine().getUri(),
										GetStringOfInputStream(((HttpEntityEnclosingRequest) request).getEntity()
												.getContent())));
							}
						}
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

		public boolean isRun() {
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

		public void setAuthorizedUserPass(String user_pass) {
			this.user_pass = user_pass;
		}
	}

	public static class Zeroconf extends Thread {
		public Observable<Message> observer;

		private Context context;
		private DatagramSocket socket;
		private boolean run = false;
		private int port = 0;

		public Zeroconf(Context context) {
			this.context = context;
			observer = new Observable<Message>();
		}

		public boolean isRun() {
			return run;
		}

		private InetAddress getBroadcastAddress() throws IOException {
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
				DatagramPacket packet = new DatagramPacket(data, data.length, getBroadcastAddress(), port);

				Log.d(MainApplication.RH_TAG, "Send hello: " + packet.getAddress().toString() + ":" + packet.getPort());

				socket.send(packet);
				socket.receive(packet);

				while (run) {
					byte[] buf = new byte[13];
					packet = new DatagramPacket(buf, buf.length);
					socket.receive(packet);

					if ((new String(packet.getData(), "UTF-8")).equals("hello backend")) {
						observer.notifyObservers(new Message(Type.zeroconfig, null, packet.getAddress().toString()
								+ ":" + packet.getPort()));

						Log.d(MainApplication.RH_TAG, "Hello received: " + packet.getAddress().toString() + ":"
								+ packet.getPort());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// X509TrustManager tm = new X509TrustManager() {
	// public void checkClientTrusted(X509Certificate[] xcs, String
	// string) throws CertificateException {
	// }
	//
	// public void checkServerTrusted(X509Certificate[] xcs, String
	// string) throws CertificateException {
	// }
	//
	// public X509Certificate[] getAcceptedIssuers() {
	// return null;
	// }
	// };
	// client = new DefaultHttpClient();
	// SSLContext clientCtx = SSLContext.getInstance("TLS");
	// clientCtx.init(null, new TrustManager[] { tm }, null);
	// SSLSocketFactory ssf = new MySSLSocketFactory(clientCtx);
	// ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	// ClientConnectionManager ccm = client.getConnectionManager();
	// SchemeRegistry sr = ccm.getSchemeRegistry();
	// sr.register(new Scheme("https", ssf, 443));
	// client = new DefaultHttpClient(ccm, client.getParams());

	// private class MySSLSocketFactory extends SSLSocketFactory {
	// SSLContext sslContext = SSLContext.getInstance("TLS");
	//
	// public MySSLSocketFactory(SSLContext context) throws
	// KeyManagementException, NoSuchAlgorithmException,
	// KeyStoreException, UnrecoverableKeyException {
	// super(null);
	// sslContext = context;
	// }
	//
	// @Override
	// public Socket createSocket(Socket socket, String host, int port, boolean
	// autoClose) throws IOException,
	// UnknownHostException {
	// return sslContext.getSocketFactory().createSocket(socket, host, port,
	// autoClose);
	// }
	//
	// @Override
	// public Socket createSocket() throws IOException {
	// return sslContext.getSocketFactory().createSocket();
	// }
	// }

}
