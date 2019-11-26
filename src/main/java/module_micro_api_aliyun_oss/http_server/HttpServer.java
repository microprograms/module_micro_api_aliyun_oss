package module_micro_api_aliyun_oss.http_server;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import module_micro_api_aliyun_oss.Config;

public class HttpServer {
	private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

	private Server server;
	private Config config;

	public HttpServer(Config config) {
		this.config = config;
	}

	public synchronized void start() throws Exception {
		server = new Server(Integer.valueOf(config.getPort()));
		ServletContextHandler webApiContext = new ServletContextHandler();
		webApiContext.setContextPath("/");
		webApiContext.addServlet(new ServletHolder(new OssServlet(config)), "/api/*");
		webApiContext.addServlet(new ServletHolder(new OssDownloadServlet(config)), "/download/*");
		webApiContext.setSessionHandler(new SessionHandler());

		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { webApiContext, new DefaultHandler() });
		server.setHandler(handlers);
		server.start();
	}

	public synchronized void stop() throws Exception {
		server.stop();
	}
}
