package module_micro_api_aliyun_oss.utils;

import java.sql.SQLException;

import module_micro_api_aliyun_oss.Config;

public class R {
	private static boolean inited = false;
	private static Config config;

	public static synchronized void init(Config config) throws SQLException {
		if (inited) {
			return;
		}

		R.config = config;

		inited = true;
	}

	public static Config getConfig() {
		return config;
	}
}
