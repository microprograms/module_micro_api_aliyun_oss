package module_micro_api_aliyun_oss;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
	private static final Logger log = LoggerFactory.getLogger(Config.class);

	private static final String property_key_port = "module_micro_api_aliyun_oss.port";
	private static final String property_key_callback_url = "module_micro_api_aliyun_oss.callback_url";
	private static final String property_key_access_id = "module_micro_api_aliyun_oss.access_id";
	private static final String property_key_access_key = "module_micro_api_aliyun_oss.access_key";
	private static final String property_key_endpoint = "module_micro_api_aliyun_oss.endpoint";
	private static final String property_key_bucket = "module_micro_api_aliyun_oss.bucket";
	private static final String property_key_dir = "module_micro_api_aliyun_oss.dir";

	private String port;
	private String accessId;
	private String accessKey;
	private String endpoint;
	private String bucket;
	private String dir;
	private String callbackUrl;

	public synchronized static Config load(BundleContext context) {
		Config config = new Config();

		String port = context.getProperty(property_key_port);
		log.info("BundleContext Property {} = {}", property_key_port, port);
		config.setPort(port);

		String callbackUrl = context.getProperty(property_key_callback_url);
		log.info("BundleContext Property {} = {}", property_key_callback_url, callbackUrl);
		config.setCallbackUrl(callbackUrl);

		String accessId = context.getProperty(property_key_access_id);
		log.info("BundleContext Property {} = {}", property_key_access_id, accessId);
		config.setAccessId(accessId);

		String accessKey = context.getProperty(property_key_access_key);
		log.info("BundleContext Property {} = {}", property_key_access_key, accessKey);
		config.setAccessKey(accessKey);

		String endpoint = context.getProperty(property_key_endpoint);
		log.info("BundleContext Property {} = {}", property_key_endpoint, endpoint);
		config.setEndpoint(endpoint);

		String bucket = context.getProperty(property_key_bucket);
		log.info("BundleContext Property {} = {}", property_key_bucket, bucket);
		config.setBucket(bucket);

		String dir = context.getProperty(property_key_dir);
		log.info("BundleContext Property {} = {}", property_key_dir, dir);
		config.setDir(dir);

		return config;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getAccessId() {
		return accessId;
	}

	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}
}
