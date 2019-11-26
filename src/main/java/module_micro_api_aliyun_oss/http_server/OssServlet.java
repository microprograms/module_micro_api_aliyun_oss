package module_micro_api_aliyun_oss.http_server;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;

import module_micro_api_aliyun_oss.Config;
import net.sf.json.JSONObject;

@WebServlet(asyncSupported = true)
public class OssServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(OssServlet.class);

	private Config config;

	public OssServlet(Config config) {
		this.config = config;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		@SuppressWarnings("deprecation")
		OSSClient client = new OSSClient(config.getEndpoint(), config.getAccessId(), config.getAccessKey());
		try {
			long expireTime = 30;
			long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
			Date expiration = new Date(expireEndTime);
			PolicyConditions policyConds = new PolicyConditions();
			policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
			policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, config.getDir());

			String postPolicy = client.generatePostPolicy(expiration, policyConds);
			byte[] binaryData = postPolicy.getBytes("utf-8");
			String encodedPolicy = BinaryUtil.toBase64String(binaryData);
			String postSignature = client.calculatePostSignature(postPolicy);

			Map<String, String> respMap = new LinkedHashMap<String, String>();
			respMap.put("accessid", config.getAccessId());
			respMap.put("policy", encodedPolicy);
			respMap.put("signature", postSignature);
			respMap.put("dir", config.getDir());
			respMap.put("host", "http://" + config.getBucket() + "." + config.getEndpoint());
			respMap.put("expire", String.valueOf(expireEndTime / 1000));

			JSONObject jsonCallback = new JSONObject();
			jsonCallback.put("callbackUrl", config.getCallbackUrl());
			jsonCallback.put("callbackBody",
					"filename=${object}&size=${size}&mimeType=${mimeType}&height=${imageInfo.height}&width=${imageInfo.width}");
			jsonCallback.put("callbackBodyType", "application/x-www-form-urlencoded");
			String base64CallbackBody = BinaryUtil.toBase64String(jsonCallback.toString().getBytes());
			respMap.put("callback", base64CallbackBody);

			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Methods", "GET, POST");
			response(request, response, JSONObject.fromObject(respMap).toString());

		} catch (Exception e) {
			log.error("", e);
		}
	}

	private static void response(HttpServletRequest request, HttpServletResponse response, String results)
			throws IOException {
		String callbackFunName = request.getParameter("callback");
		if (callbackFunName == null || callbackFunName.equalsIgnoreCase("")) {
			response.getWriter().println(results);
		} else {
			response.getWriter().println(callbackFunName + "( " + results + " )");
		}
		response.setStatus(HttpServletResponse.SC_OK);
		response.flushBuffer();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String ossCallbackBody = getPostBody(request.getInputStream(),
				Integer.parseInt(request.getHeader("content-length")));
		if (verifyOssCallbackRequest(request, ossCallbackBody)) {
			log.info("oss callback body: {}", ossCallbackBody);
			response(request, response, "{\"Status\":\"OK\"}", HttpServletResponse.SC_OK);
		} else {
			response(request, response, "{\"Status\":\"verdify not ok\"}", HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	private static String getPostBody(InputStream is, int contentLen) {
		if (contentLen > 0) {
			int readLen = 0;
			int readLengthThisTime = 0;
			byte[] message = new byte[contentLen];
			try {
				while (readLen != contentLen) {
					readLengthThisTime = is.read(message, readLen, contentLen - readLen);
					if (readLengthThisTime == -1) {// Should not happen.
						break;
					}
					readLen += readLengthThisTime;
				}
				return new String(message);
			} catch (IOException e) {
				log.error("", e);
			}
		}
		return "";
	}

	private static boolean verifyOssCallbackRequest(HttpServletRequest request, String ossCallbackBody)
			throws NumberFormatException, IOException {
		boolean ret = false;
		String autorizationInput = new String(request.getHeader("Authorization"));
		String pubKeyInput = request.getHeader("x-oss-pub-key-url");
		byte[] authorization = BinaryUtil.fromBase64String(autorizationInput);
		byte[] pubKey = BinaryUtil.fromBase64String(pubKeyInput);
		String pubKeyAddr = new String(pubKey);
		if (!pubKeyAddr.startsWith("http://gosspublic.alicdn.com/")
				&& !pubKeyAddr.startsWith("https://gosspublic.alicdn.com/")) {
			log.error("pub key addr must be oss addrss");
			return false;
		}
		String retString = executeGet(pubKeyAddr);
		retString = retString.replace("-----BEGIN PUBLIC KEY-----", "");
		retString = retString.replace("-----END PUBLIC KEY-----", "");
		String queryString = request.getQueryString();
		String uri = request.getRequestURI();
		String decodeUri = URLDecoder.decode(uri, "UTF-8");
		String authStr = decodeUri;
		if (queryString != null && !queryString.equals("")) {
			authStr += "?" + queryString;
		}
		authStr += "\n" + ossCallbackBody;
		ret = doCheck(authStr, authorization, retString);
		return ret;
	}

	public static String executeGet(String url) {
		DefaultHttpClient client = null;
		InputStream content = null;
		try {
			client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			request.setURI(new URI(url));
			HttpResponse response = client.execute(request);
			content = response.getEntity().getContent();
			return StringUtils.join(IOUtils.readLines(content, "utf8"), "\n");
		} catch (Exception e) {
			return null;
		} finally {
			IOUtils.closeQuietly(content);
			IOUtils.closeQuietly(client);
		}
	}

	private static boolean doCheck(String content, byte[] sign, String publicKey) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			byte[] encodedKey = BinaryUtil.fromBase64String(publicKey);
			PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
			Signature signature = Signature.getInstance("MD5withRSA");
			signature.initVerify(pubKey);
			signature.update(content.getBytes());
			return signature.verify(sign);
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}

	private static void response(HttpServletRequest request, HttpServletResponse response, String results, int status)
			throws IOException {
		String callbackFunName = request.getParameter("callback");
		response.addHeader("Content-Length", String.valueOf(results.length()));
		if (callbackFunName == null || callbackFunName.equalsIgnoreCase("")) {
			response.getWriter().println(results);
		} else {
			response.getWriter().println(callbackFunName + "( " + results + " )");
		}
		response.setStatus(status);
		response.flushBuffer();
	}
}