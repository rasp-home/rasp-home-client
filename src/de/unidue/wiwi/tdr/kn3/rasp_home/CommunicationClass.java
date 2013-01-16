package de.unidue.wiwi.tdr.kn3.rasp_home;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
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
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

public class CommunicationClass extends Thread {

	public Observable<String> observer;

	private Context context;

	private boolean serverRun = false;
	private String username = "";
	private String password = "";

	private HttpService service;
	private HttpRequestHandlerRegistry registry;
	private SSLServerSocketFactory socketFactory;
	private SSLServerSocket serverSocket;

	private DefaultHttpClient client;

	public CommunicationClass(Context context) {
		this.context = context;
		observer = new Observable<String>();

		service = new HttpService(new BasicHttpProcessor(), new DefaultConnectionReuseStrategy(),
				new DefaultHttpResponseFactory());
		registry = new HttpRequestHandlerRegistry();
		registry.register("*", new HttpRequestHandler() {
			@Override
			public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException,
					IOException {
				Header authHeader = request.getFirstHeader("Authorization");
				if (authHeader != null) {
					if (authHeader.getValue().startsWith("Basic ")) {
						String auth = new String(Base64.decode(authHeader.getValue().substring(6), Base64.DEFAULT),
								"UTF-8");
						if (authUser(auth)) {
							Uri uri = Uri.parse(request.getRequestLine().getUri());
							if (request.getRequestLine().getMethod().equals("GET")) {
								Log.d(MainApplication.RH_TAG, "Request GET");
								observer.notifyObservers("Request GET " + uri.toString());
								response.addHeader("Content-Type", "text/txt");
								response.setEntity(new StringEntity("Test", "UTF-8"));
							} else if (request.getRequestLine().getMethod().equals("PUT")) {
								String requestString = getStringOfEntity(((HttpEntityEnclosingRequest) request)
										.getEntity());
								Log.d(MainApplication.RH_TAG, "Request PUT");
								observer.notifyObservers("Request PUT " + uri.toString() + " " + requestString);
							}

							return;
						}
					}
				}

				response.addHeader("WWW-Authenticate", "BASIC realm=\"Authentication\"");
				response.setStatusCode(401);
			}
		});
		service.setHandlerResolver(registry);

		try {
			SSLContext serverCtx = SSLContext.getInstance("TLS");
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(this.context.getAssets().open("server.keystore"), "rhclien".toCharArray());
			kmf.init(keystore, "rhclien".toCharArray());
			serverCtx.init(kmf.getKeyManagers(), null, null);
			socketFactory = serverCtx.getServerSocketFactory();

			X509TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			client = new DefaultHttpClient();
			SSLContext clientCtx = SSLContext.getInstance("TLS");
			clientCtx.init(null, new TrustManager[] { tm }, null);
			SSLSocketFactory ssf = new MySSLSocketFactory(clientCtx);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = client.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", ssf, 443));
			client = new DefaultHttpClient(ccm, client.getParams());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setUsernamePassword(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public boolean authUser(String auth) {
		if (auth.equals("name:pass")) {
			return true;
		} else {
			return false;
		}
	}

	public boolean StartServer(int port) {
		if (!serverRun) {
			try {
				serverSocket = (SSLServerSocket) socketFactory.createServerSocket(8008);
				serverSocket.setReuseAddress(true);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			super.start();
			serverRun = true;
			return true;
		} else {
			return false;
		}
	}

	public boolean StopServer() {
		if (serverRun) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			serverRun = false;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void run() {
		super.run();
		while (serverRun) {
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
	
	public boolean sendRequest(String uri) {
		HttpGet request = new HttpGet(uri);
		return sendRequest(request);
	}

	public boolean sendRequest(String uri, String content) {
		HttpPut request = new HttpPut(uri);
		request.addHeader("Content-Type", "text/txt");
		try {
			((HttpPut) request).setEntity(new StringEntity(content, "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return sendRequest(request);
	}

	private boolean sendRequest(HttpUriRequest request) {
		try {
			UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password);
			request.addHeader(BasicScheme.authenticate(creds, "UTF-8", false));
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == 200) {
				Header contentHeader = response.getFirstHeader("Content-Type");
				if (contentHeader != null) {
					if (contentHeader.getValue().equals("text/txt")) {
						String responseString = getStringOfEntity(response.getEntity());
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

	private String getStringOfEntity(HttpEntity entity) {
		try {
			InputStreamReader reader = new InputStreamReader(entity.getContent(), "UTF-8");
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

	private class MySSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public MySSLSocketFactory(SSLContext context) throws KeyManagementException, NoSuchAlgorithmException,
				KeyStoreException, UnrecoverableKeyException {
			super(null);
			sslContext = context;
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,
				UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}

}
