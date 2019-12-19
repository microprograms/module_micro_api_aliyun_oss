package module_micro_api_aliyun_oss;

import com.github.microprograms.osgi_module_activator.ModuleActivator;
import module_micro_api_aliyun_oss.api.AliyunOss_CreatePresignedUrl;
import module_micro_api_aliyun_oss.http_server.HttpServer;
import module_micro_api_aliyun_oss.utils.R;

public class Activator extends ModuleActivator {

    private HttpServer httpServer;

    @Override
    protected void onStart() throws Exception {
        R.init(Config.load(context));
        httpServer = new HttpServer();
        httpServer.start();
        registerApis();
    }

    @Override
    protected void onStop() throws Exception {
        httpServer.stop();
    }

    private void registerApis() {
        registerApi(new AliyunOss_CreatePresignedUrl());
    }
}
