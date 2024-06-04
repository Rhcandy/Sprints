package mg.itu.prom16;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.*;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import mg.itu.prom16.annotation.*;
import mg.itu.prom16.util.*;

public class FrontController extends HttpServlet {
    private List<Class<?>> classes;
    private String basePackageName;
    private Map<String, Mapping> urlMappings = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        classes = new ArrayList<>();
        basePackageName = config.getInitParameter("packageTest");

        try {
            initVariable();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void initVariable() throws Exception {
        classes = ClassScanner.scanClasses(basePackageName, Controller.class);
        for (Class<?> controller : classes) {
            Method[] methods = controller.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Get.class)) {
                    Get getAnnotation = method.getAnnotation(Get.class);
                    String url = getAnnotation.value();
                    Mapping mapping = new Mapping(controller.getName(), method.getName());
                    urlMappings.put(url, mapping);
                }
            }
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

    response.setContentType("text/html");
    PrintWriter out = response.getWriter();

    // Récupérer l'URL après le port et le host
    // String requestURI = request.getRequestURI();
    // String contextPath = request.getContextPath();

    // Retirer le contexte de l'application si nécessaire
    // String relativeURI = requestURI.substring(contextPath.length());
    String relativeURI = request.getServletPath();

    Mapping mapping = urlMappings.get(relativeURI);

    for (String key : urlMappings.keySet()) {
        out.println("<html><head><title>Servlet Response</title></head><body>");
        out.println("<p> URL: " + key + "  mapping: "+urlMappings.get(key).getClassName()+" | "+ urlMappings.get(key).getMethodName()  +"</p>");
        out.println("</body></html>");
    }

    
    if (mapping != null) {
        Class<?> controllClass = Class.forName(mapping.getClassName());
        Object controllerInstance = controllClass.getDeclaredConstructor().newInstance();
        Method method = controllClass.getMethod(mapping.getMethodName());
        Object result = method.invoke(controllerInstance);

        if (result instanceof String) {
            out.println("<html><head><title>Servlet Response</title></head><body>");
            out.println("<p>hgsndckjbwnnBWxnbhVQcw</p>");
            out.println("<p>" + result + "</p>");
            out.println("</body></html>");
        } else if (result instanceof ModelView) {
            ModelView modelView = (ModelView) result;
            for (Map.Entry<String, Object> entry : modelView.getData().entrySet()) {
                request.setAttribute(entry.getKey(), entry.getValue());
            }
            request.getRequestDispatcher(modelView.getUrl()).forward(request, response);
        } else {
            out.println("<html><head><title>Servlet Response</title></head><body>");
            out.println("<p>Type de retour non reconnu</p>");
            out.println("</body></html>");
        }
    } else {
        out.println("<html><head><title>Servlet Response</title></head><body>");
        out.println("<p>No method associated with this URL: " + relativeURI + "</p>");
        out.println("</body></html>");
    }
}

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException | ServletException
                | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException | ServletException
                | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
