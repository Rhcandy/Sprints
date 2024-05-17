package mg.itu.prom16;

import java.io.IOException;
import java.io.PrintWriter;
import annotation.Controller;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.util.ClassScanner;

public class FrontController extends HttpServlet {
    private boolean isScanned; 
    private List<Class<?>> classes;
    private String basePackageName;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        isScanned = false;
        classes = new ArrayList<Class<?>>();
        basePackageName = config.getInitParameter("packageTest");
    }

    protected void print(HttpServletResponse response) throws IOException{
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><head><title>Servlet Response</title></head><body>");
        out.println("<p>Hello ! </p>");
        out.println("</body></html>");
    }

    protected void initVariable() throws Exception {
        try{
            classes = ClassScanner.scanClasses(basePackageName, Controller.class);
            isScanned = true;
        }
        catch(Exception e){
            isScanned = false;
            throw new ServletException(e);
        }
    }

    // 
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Afficher quelques choses
        print(response);

        PrintWriter out = response.getWriter();
        
        // Récupérer l'URL tapée par l'utilisateur
        StringBuffer url = request.getRequestURL();

        // Récupérer l'URL apres le port et le host
        // Récupérer le contexte (nom) de l'application
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();

        // Retirer le contexte de l'application si nécessaire
        String relativeURI = requestURI.substring(contextPath.length());

        // Lecture du nom package dans dispacther-servlet  
        try {
            System.out.println("Le nom du package : " + basePackageName);

            if (!isScanned) {
                initVariable();
            }

            out.println("Les Controllers disponibles : ");

            out.println("<ul>");
            for (Class<?> class1 : classes) {
                out.println("<li>" + class1.getSimpleName() + "</li>");
            }
            out.println("</ul>");
        } 
        
        catch (Exception e) {
            String path_error_page = "\\WEB-INF\\error_page.jsp";
            
            request.setAttribute("url_not_found", requestURI);
            request.getRequestDispatcher(path_error_page).forward(request, response);

            System.out.println("Exception: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

}
