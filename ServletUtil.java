package mg.itu.prom16.util;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import mg.itu.prom16.annotation.ModelParam;
import mg.itu.prom16.annotation.MultiPartFile;
import mg.itu.prom16.annotation.RequestFile;
import mg.itu.prom16.annotation.RequestParam;
import mg.itu.prom16.exception.InvalidConstraintException;
import mg.itu.prom16.validation.BindingResult;
import mg.itu.prom16.validation.FieldError;
import mg.itu.prom16.validation.annotation.Valid;
import mg.itu.prom16.validation.constraints.Email;
import mg.itu.prom16.validation.constraints.Max;
import mg.itu.prom16.validation.constraints.Min;
import mg.itu.prom16.validation.constraints.NotBlank;
import mg.itu.prom16.validation.constraints.Size;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ServletUtil {
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    public static boolean isValidEmail(String email) {
        return Pattern.matches(EMAIL_REGEX, email);
    }

    // validation
    public static void setParamsModel(HttpServletRequest request, Object o, String valParam, boolean haveValidAnnot, List<FieldError> errors) throws Exception {
        for (Field atr : o.getClass().getDeclaredFields()) {
            atr.setAccessible(true);
            String val = request.getParameter(valParam + "." + atr.getName());
            Object valeurATr = parseValue(val, atr.getType());
            atr.set(o, valeurATr);
            if (haveValidAnnot) {
                checkValidation(atr, o, errors);
            }
        }
    }

    public static String getErrorsStr(List<FieldError> errors) {
        StringBuilder errorMessages = new StringBuilder();
        errorMessages.append("\n");
        
        // Parcourir la liste des erreurs
        for (FieldError error : errors) {
            errorMessages.append("Champ : ").append(error.getField()).append("\n")
                         .append("Message : ").append(error.getErrorMessage()).append("\n");
        }
        
        // Retourner toutes les erreurs sous forme de chaîne
        return errorMessages.toString();
    }


    public static void checkValidation(Field atr, Object o, List<FieldError> fieldErrors) throws InvalidConstraintException, Exception {
        if (atr.isAnnotationPresent(NotBlank.class)) {
            Object value = atr.get(o);

            if (value instanceof String) {
                String stringValue = (String) value;
                String message = atr.getAnnotation(NotBlank.class).message();

                if (stringValue.trim().isEmpty()) {
                    fieldErrors.add(new FieldError(atr.getName(), message, stringValue, "NotBlank"));
                }
            } else {
                fieldErrors.add(new FieldError(atr.getName(),"Le champ annoté avec @NotBlank doit être de type String." ));
            }
        }
        if (atr.isAnnotationPresent(Min.class)) {
            Object value = atr.get(o);
            Min minAnnot = atr.getAnnotation(Min.class); // Obtenir l'annotation @Min

            if (value instanceof Number) {
                double d = ((Number) value).doubleValue();
                if (d < minAnnot.value()) {
                    fieldErrors.add(new FieldError(atr.getName(), minAnnot.message(), d, "@Min"));
                }
            } else {
                fieldErrors.add(new FieldError(atr.getName(),"Le champ annoté avec @Min doit être de type numérique." ));
            }
        }
        if (atr.isAnnotationPresent(Max.class)) {
            Object value = atr.get(o);
            Max maxAnnot = atr.getAnnotation(Max.class); // Obtenir l'annotation @Min

            if (value instanceof Number) {
                double d = ((Number) value).doubleValue();
                if (d > maxAnnot.value()) {
                    fieldErrors.add(new FieldError(atr.getName(), maxAnnot.message(), d, "@Max"));
                }
            } else {
                fieldErrors.add(new FieldError(atr.getName(),"Le champ annoté avec @Max doit être de type numérique." ));
            }
        } 
        if (atr.isAnnotationPresent(Email.class)) {
            Object value = atr.get(o);

            if (value instanceof String) {
                boolean emailValid = isValidEmail(value.toString());
                if (!emailValid) {

                    Email emailAnnot = atr.getAnnotation(Email.class);
                    fieldErrors.add(new FieldError(atr.getName(), emailAnnot.message(), value, "@Email"));
                }
            } else {
                fieldErrors.add(new FieldError(atr.getName(),"Le champ annoté avec @Email doit être de type String." ));
            }
        }
        if (atr.isAnnotationPresent(Size.class)) {
            Object value = atr.get(o);
            if (value instanceof String || value instanceof Collection 
                || value instanceof Object[] || value instanceof Map || value instanceof List 
                || value instanceof ArrayList) 
            {
                Size sizeAnnot = atr.getAnnotation(Size.class);

                if (value instanceof String) {
                    int nombreCaracteres = value.toString().length();
                    if (sizeAnnot.min() > nombreCaracteres ||  sizeAnnot.max() < nombreCaracteres) {
                        fieldErrors.add(new FieldError(atr.getName(), sizeAnnot.message(), value, "@Size"));
                    }
                } else if (value instanceof List) {
                    int nombreCaracteres = ((List) value).size();
                    if (sizeAnnot.min() > nombreCaracteres ||  sizeAnnot.max() < nombreCaracteres) {
                        fieldErrors.add(new FieldError(atr.getName(), sizeAnnot.message(), value, "@Size"));
                    }
                }
            }
        }
    }
    // end valdation

    public static List<Object> parseParameters(HttpServletRequest request, Method method) throws Exception {
        List<Object> parsedArgs = new ArrayList<>();
        List<FieldError> fieldErrors = new ArrayList<>();
        boolean validAnnotExist = false;
        for (Parameter arg : method.getParameters()) {
            
            if (arg.getType().equals(MySession.class)) {
                Object object = MySession.class.getDeclaredConstructor().newInstance();
                MySession session = (MySession) object;
                session.setSession(request.getSession());
                parsedArgs.add(session);
                continue;
            }

            if (arg.isAnnotationPresent(RequestFile.class)) {
                setMultipartFile(arg, request, parsedArgs);
                continue;
            }

            String annotName;
            Object value = null;
            RequestParam requestParam = arg.getAnnotation(RequestParam.class);
            ModelParam modelParam = arg.getAnnotation(ModelParam.class);

            if (modelParam != null) {
                Valid valid = arg.getAnnotation(Valid.class);
                validAnnotExist = true;
                String valueParam = modelParam.value();
                if (valueParam.isEmpty()) {
                    valueParam = arg.getName();
                }

                Class<?> paramaType = arg.getType();
                Constructor<?> constructor = paramaType.getDeclaredConstructor();
                Object instance = constructor.newInstance();
                setParamsModel(request, instance, valueParam, valid != null, fieldErrors); // nouveau
                value = instance;
            }
            else if (requestParam != null) {
                if (requestParam.value().isEmpty()) {
                    annotName = arg.getName();
                }else {
                    annotName = requestParam.value();
                }
                value = request.getParameter(annotName);
            } 
            else if (arg.getType().equals(BindingResult.class) && validAnnotExist) {
                BindingResult br = getBindingResult(fieldErrors);
                parsedArgs.add(br);
                continue;
            }
            else {
                throw new Exception("Annotation not found");
            }
            parsedArgs.add(value);
        }
        return parsedArgs;
    }

    public static Object invokeMethod(Mapping mapping , HttpServletRequest request, String verb) throws Exception {
        ApiRequest apiRequest = mapping.getRequest(verb);
        Method method = apiRequest.getMethod();

        Object instance = apiRequest.getClass1().getDeclaredConstructor().newInstance();
        List<Object> listArgs = parseParameters(request, method);
        putSession(request,  instance);
        Object valueFunction = method.invoke(instance, listArgs.toArray());
        return valueFunction;
    }

    private static BindingResult getBindingResult(List<FieldError> fieldErrors) {
        return new BindingResult(fieldErrors);
    }

    private static void setMultipartFile(Parameter argParameter, HttpServletRequest request, List<Object> values) throws Exception {
        RequestFile requestFile = argParameter.getAnnotation(RequestFile.class);
        String nameFileInput = "";
        if (requestFile == null || requestFile.value().isEmpty()) {
            nameFileInput = argParameter.getName();
        }
        else {
            nameFileInput = requestFile.value();
        }
    
        Part part = request.getPart(nameFileInput);
        if (part == null) {
            values.add(null);
            return;
        }

        if (argParameter.getType().isAssignableFrom(MultiPartFile.class)) {
            Class<?> paramaType = argParameter.getType();
            Constructor<?> constructor = paramaType.getDeclaredConstructor();
            Object o = constructor.newInstance();
        
            MultiPartFile multiPartFile = (MultiPartFile) o;
            multiPartFile.buildInstance(part, "1859");
            values.add(multiPartFile);
        } else {
            throw new Exception("Parameter not valid Exception for File!");
        }
    }

    private static Object parseValue(String value, Class<?> type) {
        if (type.equals(String.class)) {
            return value;
        } else if (type.equals(Integer.class) || type.equals(int.class)) {
            return Integer.parseInt(value);
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return Double.parseDouble(value);
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return Boolean.parseBoolean(value);
        }
        else {
            throw new IllegalArgumentException("Type de paramètre non supporté: " + type);
        }
    }

    public static void putSession(HttpServletRequest request, Object obj) throws Exception {
       Field[] fields = obj.getClass().getDeclaredFields();
       
       for (Field field : fields) {
            if (field.getType().equals(MySession.class)) {
                field.setAccessible(true);
                Object object = field.get(obj);

                if (object == null) {
                    object = MySession.class.getDeclaredConstructor().newInstance();
                    field.set(obj, object);
                    MySession session = (MySession) object;
                    session.setSession(request.getSession());
                    break;
                }
            }
       }
    }
}
