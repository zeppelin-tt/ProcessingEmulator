package servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/ProcessingServlet")
public class ProcessingServlet extends HttpServlet {
    private static String index = "/WEB-INF/web.xml";

    protected void doGet (HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
//        request.getRequestDispatcher(index).forward(request, response);
        response.getWriter().append("Hi! " + request.getMethod());
    }

}
