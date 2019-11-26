package module_micro_api_aliyun_oss;

import com.github.microprograms.osgi_module_activator.ModuleActivator;

import module_micro_api_aliyun_oss.api.CreatePresignedUrl;
import module_micro_api_aliyun_oss.http_server.HttpServer;

public class Activator extends ModuleActivator {
	private HttpServer httpServer;

	@Override
	protected void onStart() throws Exception {
		Config config = Config.load(context);
		httpServer = new HttpServer(config);
		httpServer.start();

		registerApi(new CreatePresignedUrl(config));
	}

	@Override
	protected void onStop() throws Exception {
		httpServer.stop();
	}
}
