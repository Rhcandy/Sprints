package mg.itu.prom16.util;

public class Mapping {
    private String  className;
    private String methodName;

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public Mapping(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }
    @Override
    public String toString(){
        return "className="+className+"methodName="+methodName;
    }
}
