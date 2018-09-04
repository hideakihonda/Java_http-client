package http.client.util;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.URL;

import java.util.Properties;

public class UtilPropertyManager {

	private Properties prop;

	public Properties getProp() {
		return prop;
	}

	public void setProp(Properties prop) {
		this.prop = prop;
	}

	public UtilPropertyManager(String resorceName) {
		doFactory(resorceName);
	}

	private void doFactory(String resorceName) {
		try {
			URL url = getResource(resorceName);
			prop = new Properties();
			prop.load(url.openStream());
		} catch (FileNotFoundException e) {
			System.err.println("FileNotFoundException : " + e);
		} catch (IOException e) {
			System.err.println("IOException : " + e);
		}

	}

	public URL getResource(String target) {
		ClassLoader loader = this.getClass().getClassLoader(); // クラスローダ
		URL url = null; // URL
		for (; loader != null;) {
			url = loader.getResource(target);
			if (url == null) {
				// 見つからない場合は、親の親のクラスローダを検索対象とする
				loader = loader.getParent();
			} else {
				// 見つかった場合は、ループを抜ける
				break;
			}
		}
		return url;
	}

	public String getMessage(String key) {
		return prop.getProperty(key);
	}
}
