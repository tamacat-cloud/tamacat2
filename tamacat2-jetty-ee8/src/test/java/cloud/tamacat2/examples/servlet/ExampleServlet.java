package cloud.tamacat2.examples.servlet;

import java.io.IOException;

import cloud.tamacat2.httpd.util.StringUtils;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ExampleServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.getWriter().print("test");
		int code = StringUtils.parse(req.getParameter("error"), -1);
		if (code >= 400 && code < 600) {
			resp.sendError(code);
		}
	}
}
