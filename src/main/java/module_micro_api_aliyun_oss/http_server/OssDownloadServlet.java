package module_micro_api_aliyun_oss.http_server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;

import module_micro_api_aliyun_oss.Config;

@WebServlet(asyncSupported = true)
public class OssDownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(OssDownloadServlet.class);

	private Config config;

	public OssDownloadServlet(Config config) {
		this.config = config;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		OutputStream output = resp.getOutputStream();
		OSS ossClient = null;
		InputStream ossInputStream = null;
		try {
			ossClient = new OSSClientBuilder().build(config.getEndpoint(), config.getAccessId(), config.getAccessKey());
			String objectName = req.getParameter("objectName");
			OSSObject ossObject = ossClient.getObject(config.getBucket(), objectName);
			_setHeaders(ossObject.getObjectMetadata(), objectName, resp);
			ossInputStream = ossObject.getObjectContent();
			IOUtils.copy(ossInputStream, output);
		} finally {
			IOUtils.closeQuietly(ossInputStream);
			if (null != ossClient) {
				ossClient.shutdown();
			}
			IOUtils.closeQuietly(output);
		}
	}

	private static void _setHeaders(ObjectMetadata metadata, String objectName, HttpServletResponse resp)
			throws UnsupportedEncodingException {
		// 设置跨域响应头
		resp.setHeader("Access-Control-Allow-Origin", "*");
		resp.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
		resp.setHeader("Access-Control-Max-Age", "3600");
		resp.setHeader("Access-Control-Allow-Headers", "x-requested-with, Content-Type");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		// 设置文件下载相关的响应头
		resp.setContentType(metadata.getContentType());
		String contentDisposition = "attachment;filename*=UTF-8''" + URLEncoder.encode(objectName, "UTF-8");
		resp.setHeader("Content-Disposition", contentDisposition);
	}
}
