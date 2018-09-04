package http.client.main;

import http.client.util.HttpAuthenticator;
import http.client.util.UtilPropertyManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * http/https client class
 * @author hideaki honda
 * @version 1.0
 */
public class HttpClient {

	private static Logger log = Logger.getLogger(HttpClient.class);

	/**
	 * main method
	 * @param arg[0] PropertyFile Name
	 * @param arg[1] request Parameter
	 * @return void
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure("");

		System.out.println();

		String PropertyFileName = null;
		String requestParameter = null;

		if (args.length == 0) {
			log.error("no argument");

		} else if (args.length == 1) {
			PropertyFileName = args[0];

		} else if (args.length == 2) {
			PropertyFileName = args[0];
			requestParameter = args[1];
		}

		log.info("http.client.main.HttpClient start --------------------------");
		log.info("http.client.main.HttpClient args[0] -> " + PropertyFileName);
		log.info("http.client.main.HttpClient args[1] -> " + requestParameter);

		HttpURLConnection httpConnect = null;
		URL url                       = null;
		BufferedReader bufReader      = null;
		PrintStream printStream       = null;
		UtilPropertyManager propUtil  = null;

		int status = 0;

		try {
			propUtil    = new UtilPropertyManager(PropertyFileName);
			url         = new URL(propUtil.getMessage("request.url"));
			httpConnect = (HttpURLConnection) url.openConnection();

			//set http connection
			setHttpConnection(httpConnect, propUtil.getProp());

			//set http header
			setHttpHeader(httpConnect, propUtil.getProp());

			//set basicAuthentication
			if (Boolean.parseBoolean(propUtil.getMessage("basicAuthentication.enable"))) {
				setBasicAuthentication(propUtil.getMessage("basicAuthentication.user"),
									   propUtil.getMessage("basicAuthentication.password"));
			}

			//set proxy
			if (Boolean.parseBoolean(propUtil.getMessage("proxy.enable"))) {
				setProxy(propUtil.getMessage("proxy.host"),
						 propUtil.getMessage("proxy.port"),
						 propUtil.getMessage("proxy.nonProxyHosts"));
			}

			//logging request info
			if (log.isInfoEnabled()) {
				log.info("request header [");

				for (Entry<String, List<String>> entry : httpConnect.getRequestProperties().entrySet()) {
					log.info(" " + entry.getKey() + ":" + entry.getValue());
				}

				log.info("]");
			}

			//server connect
			httpConnect.connect();

			//set request parameter
			if (requestParameter != null) {
				printStream = new PrintStream(httpConnect.getOutputStream(), true);
				printStream.print(requestParameter);
				printStream.close();
			}

			//logging response info
			if (log.isInfoEnabled()) {
				log.info("response code    [" + httpConnect.getResponseCode() + "]");
				log.info("response message [" + httpConnect.getResponseMessage() + "]");
				log.info("response header  [");

				for (Entry<String, List<String>> entry : httpConnect.getHeaderFields().entrySet()) {
					log.info(" " + entry.getKey() + ":" + entry.getValue());
				}

				log.info("]");
			}

			//logging response data
			bufReader = new BufferedReader(new InputStreamReader(httpConnect.getInputStream()));

			if (log.isInfoEnabled()) {
				StringBuilder strBuf = new StringBuilder();

				String str = null;

				while ((str = bufReader.readLine()) != null) {
					strBuf.append(str);
				}

				log.info(strBuf);
			}

		} catch (Exception e) {
			status = 1;

			for (StackTraceElement ste : e.getStackTrace()) {
				log.error(ste.toString());
			}

		} finally {

			if (printStream != null) {
				printStream.close();
			}

			if (httpConnect != null) {
				httpConnect.disconnect();
			}

			if (bufReader != null) {
				try {
					bufReader.close();
				} catch (IOException e) {
					log.error("BufferedReader can't close");
				}
			}

			log.info("http.client.main.HttpClient end --------------------------");
			System.exit(status);
		}
	}

	/**
	 * set http header
	 * @param HttpURLConnection
	 * @param Properties
	 * @return void
	 * @throws ProtocolException
	 */
	private static void setHttpConnection(HttpURLConnection httpConnect, Properties prop) throws ProtocolException {

		final String prefix                = "connection.";
		String allowuserinteraction        = prop.getProperty(prefix + "allowuserinteraction");
		String chunkedStreamingMod         = prop.getProperty(prefix + "chunkedStreamingMod");
		String connectTimeout              = prop.getProperty(prefix + "connectTimeout");
		String defaultUseCaches            = prop.getProperty(prefix + "defaultUseCaches");
		String doInput                     = prop.getProperty(prefix + "doInput");
		String doOutput                    = prop.getProperty(prefix + "doOutput");
		String fixedLengthStreamingMode    = prop.getProperty(prefix + "fixedLengthStreamingMode");
		String ifModifiedSince             = prop.getProperty(prefix + "ifModifiedSince");
		String instanceFollowRedirects     = prop.getProperty(prefix + "instanceFollowRedirects");
		String readTimeout                 = prop.getProperty(prefix + "readTimeout");
		String requestMethod               = prop.getProperty(prefix + "requestMethod");
		String useCaches                   = prop.getProperty(prefix + "useCaches");
		String defaultAllowUserInteraction = prop.getProperty(prefix + "defaultAllowUserInteraction");
		String followRedirects             = prop.getProperty(prefix + "followRedirects");

		if (isEmpty(allowuserinteraction))        httpConnect.setAllowUserInteraction(Boolean.parseBoolean(allowuserinteraction));
		if (isEmpty(chunkedStreamingMod))         httpConnect.setChunkedStreamingMode(Integer.parseInt(chunkedStreamingMod));
		if (isEmpty(connectTimeout))              httpConnect.setConnectTimeout(Integer.parseInt(connectTimeout));
		if (isEmpty(defaultUseCaches))            httpConnect.setDefaultUseCaches(Boolean.parseBoolean(defaultUseCaches));
		if (isEmpty(doInput))                     httpConnect.setDoInput(Boolean.parseBoolean(doInput));
		if (isEmpty(doOutput))                    httpConnect.setDoOutput(Boolean.parseBoolean(doOutput));
		if (isEmpty(fixedLengthStreamingMode))    httpConnect.setFixedLengthStreamingMode(Long.parseLong(fixedLengthStreamingMode));
		if (isEmpty(ifModifiedSince))             httpConnect.setIfModifiedSince(Long.parseLong(ifModifiedSince));
		if (isEmpty(instanceFollowRedirects))     httpConnect.setInstanceFollowRedirects(Boolean.parseBoolean(instanceFollowRedirects));
		if (isEmpty(readTimeout))                 httpConnect.setReadTimeout(Integer.parseInt(readTimeout));
		if (isEmpty(requestMethod))               httpConnect.setRequestMethod(requestMethod);
		if (isEmpty(useCaches))                   httpConnect.setUseCaches(Boolean.parseBoolean(useCaches));
		if (isEmpty(defaultAllowUserInteraction)) HttpURLConnection.setDefaultAllowUserInteraction(Boolean.parseBoolean(defaultAllowUserInteraction));
		if (isEmpty(followRedirects))             HttpURLConnection.setFollowRedirects(Boolean.parseBoolean(followRedirects));
	}

	/**
	 * set http header
	 * @param HttpURLConnection
	 * @param Properties
	 * @return void
	 */
	private static void setHttpHeader(HttpURLConnection httpConnect, Properties prop) {
		for (Entry<Object, Object> entry : prop.entrySet()) {
			String key   = entry.getKey().toString();
			String value = entry.getValue().toString();

			if (key.toLowerCase().startsWith("requestProperty")) {
				String[] keys = key.split("\\.");

				if (keys.length == 2) {
					httpConnect.setRequestProperty(keys[1], value);
				}
			}
		}
	}

	/**
	 * set basic authentication
	 * @param user
	 * @param password
	 * @return void
	 */
	private static void setBasicAuthentication(String user, String password) {
		HttpAuthenticator http_authenticator = new HttpAuthenticator(user, password);
		Authenticator.setDefault(http_authenticator);
	}

	/**
	 * set proxy
	 * @param host
	 * @param port
	 * @param nonHost
	 * @return void
	 */
	private static void setProxy(String host, String port, String nonHost) {
		if (isEmpty(host))    System.setProperty("http.proxyHost",     host);
		if (isEmpty(port))    System.setProperty("http.proxyPort",     port);
		if (isEmpty(nonHost)) System.setProperty("http.nonProxyHosts", nonHost);
	}

	/**
	 * check empty
	 * @param target value
	 * @return result
	 */
	private static boolean isEmpty(String value) {
		return (value != null && !"".equals(value)) ? true : false;
	}
}
