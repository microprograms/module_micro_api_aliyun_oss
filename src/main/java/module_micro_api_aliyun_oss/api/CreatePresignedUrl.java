package module_micro_api_aliyun_oss.api;

import java.net.URL;
import java.util.Date;
import com.alibaba.fastjson.JSON;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.github.microprograms.micro_api_runtime.annotations.Comment;
import com.github.microprograms.micro_api_runtime.annotations.MicroApi;
import com.github.microprograms.micro_api_runtime.annotations.Required;
import com.github.microprograms.micro_api_runtime.model.Api;
import com.github.microprograms.micro_api_runtime.model.Request;
import com.github.microprograms.micro_api_runtime.model.Response;
import module_micro_api_aliyun_oss.Config;
import java.util.List;

@MicroApi(name = "createPresignedUrl", version = "v1.0.0")
@Comment("获取oss文件的（预签名）下载地址")
public class CreatePresignedUrl implements Api {

    private Config config;

    public CreatePresignedUrl(Config config) {
        this.config = config;
    }

    @Override
    public String execute(String request) throws Exception {
        Req req = JSON.parseObject(request, Req.class);
        Resp resp = new Resp();
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(config.getEndpoint(), config.getAccessId(), config.getAccessKey());
            Date expiration = new Date(new Date().getTime() + 3600 * 1000);
            URL url = ossClient.generatePresignedUrl(config.getBucket(), req.getObjectName(), expiration);
            resp.setPresignedUrl(url.toString());
            return JSON.toJSONString(resp);
        } finally {
            if (null != ossClient) {
                ossClient.shutdown();
            }
        }
    }

    public static class Req extends Request {

        @Comment("oss文件名")
        @Required
        private String objectName;

        public String getObjectName() {
            return objectName;
        }

        public void setObjectName(String objectName) {
            this.objectName = objectName;
        }
    }

    public static class Resp extends Response {

        @Comment("（预签名）下载地址")
        @Required
        private String presignedUrl;

        public String getPresignedUrl() {
            return presignedUrl;
        }

        public void setPresignedUrl(String presignedUrl) {
            this.presignedUrl = presignedUrl;
        }
    }
}
