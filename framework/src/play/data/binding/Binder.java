package play.data.binding;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import play.exceptions.BindingException;

public class Binder {
    
    static Map<Class, SupportedType> supportedTypes = new HashMap<Class, SupportedType>();
    static {
        supportedTypes.put(Date.class, new DateBinder());
    }
            
    
    public static Object bind(String name, Class clazz, Type type, Map<String,String[]> params) {
        try {
            String[] value = params.get(name);
            if(value == null) {
                value = new String[0];
            }
            // Arrays types 
            if(clazz.isArray()) {
                Object r = Array.newInstance(clazz.getComponentType(), value.length);
                for(int i=0; i<value.length; i++) {
                    Array.set(r, i, directBind(value[i], clazz.getComponentType()));
                }
                return r;
            }          
            // Collections types
            if(Collection.class.isAssignableFrom(clazz)) {
                if(clazz.isInterface()) {
                    if(clazz.equals(List.class)) {
                        clazz = ArrayList.class;
                    }
                    if(clazz.equals(Set.class)) {
                        clazz = HashSet.class;
                    }
                    if(clazz.equals(SortedSet.class)) {
                        clazz = TreeSet.class;
                    }
                }
                Collection r = (Collection)clazz.newInstance();
                Class componentClass = String.class;
                if(type instanceof ParameterizedType) {
                    componentClass = (Class)((ParameterizedType)type).getActualTypeArguments()[0];
                }
                for(String v : value) {
                    r.add(directBind(v, componentClass));
                }
                return r;
            }
            // Simple types
            if(value.length>0) {
                return directBind(value[0], clazz);
            }
            return null;
        } catch(Exception e) {
            throw new BindingException("TODO", e);
        }
    }
    
    public static Object directBind(String value, Class clazz) {
        if (clazz.getName().equals("int") || clazz.equals(Integer.class)) {
            return Integer.parseInt(value);
        }
        if (clazz.getName().equals("double") || clazz.equals(Double.class)) {
            return Double.parseDouble(value);
        }
        if (clazz.getName().equals("short") || clazz.equals(Short.class)) {
            return Short.parseShort(value);
        }
        if (clazz.getName().equals("long") || clazz.equals(Long.class)) {
            return Long.parseLong(value);
        }
        if (clazz.getName().equals("float") || clazz.equals(Float.class)) {
            return Float.parseFloat(value);
        }
        if (clazz.getName().equals("boolean") || clazz.equals(Boolean.class)) {
            return Boolean.parseBoolean(value);
        }
        if (clazz.equals(String.class)) {
            return value;
        }
        if(supportedTypes.containsKey(clazz)) {
            return supportedTypes.get(clazz).bind(value);
        }
        return null;
    }

}
