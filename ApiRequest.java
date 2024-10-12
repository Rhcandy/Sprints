package mg.itu.prom16.util;

import java.lang.reflect.Method;

public class ApiRequest {
    // Champs de la classe
    private String verb;
    private Method method;

    // Constructeur
    public ApiRequest(String verb, Method method) {
        this.verb = verb;
        this.method = method;
    }

    // Getter pour le verbe HTTP
    public String getVerb() {
        return verb;
    }

    // Setter pour le verbe HTTP
    public void setVerb(String verb) {
        this.verb = verb;
    }

    // Getter pour la méthode
    public Method getMethod() {
        return method;
    }

    // Setter pour la méthode
    public void setMethod(Method method) {
        this.method = method;
    }

    // Méthode pour afficher les détails de la requête
    public void printRequestDetails() {
        System.out.println("HTTP Verb: " + verb);
        System.out.println("Method: " + method);
    }
}
