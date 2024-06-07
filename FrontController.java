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
import mg.itu.prom16.exception.*;

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
        }  catch (PackageNotFoundException | DuplicateUrlException e) {
            e.printStackTrace();
            throw new Error(e.getMessage());
        }
        catch (Exception ex){
            throw new ServletException(ex);
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

                    if (!urlMappings.containsKey(url)) {
                        this.urlMappings.put(url, mapping);
                    }
                    else {
                        throw new DuplicateUrlException(url);
                    }
                }
            }
        }
    }


    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            String relativeURI = request.getServletPath();
            Mapping mapping = urlMappings.get(relativeURI);

        if (mapping != null) {
            Class<?> controllClass = Class.forName(mapping.getClassName());
            Object controllerInstance = controllClass.getDeclaredConstructor().newInstance();
            Method method = controllClass.getMethod(mapping.getMethodName());
            Object result = method.invoke(controllerInstance);

            if (result instanceof String) {
                out.println("<html><head><title>Servlet Response</title></head><body>");
                out.println("<p>" + result + "</p>");
                out.println("</body></html>");
            } else if (result instanceof ModelView) {
                ModelView modelView = (ModelView) result;
                for (Map.Entry<String, Object> entry : modelView.getData().entrySet()) {
                    request.setAttribute(entry.getKey(), entry.getValue());
                }
                request.getRequestDispatcher(modelView.getUrl()).forward(request, response);
            } else {
                try {
                    throw new InvalidReturnTypeException(result.toString());
                } catch (InvalidReturnTypeException e) {
                    e.printStackTrace();
                }
                
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
