package io.scanbot.sdk.reactnative;

import android.graphics.Color;

import com.facebook.react.bridge.ReadableMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class ObjectMapper {
    private static Map<String, Map<String, Method>> methodCache = new HashMap<>();

    public static String[] map(ReadableMap source, Object target)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return map(source.toHashMap(), target);
    }

    public static String[] map(HashMap<String, Object> source, Object target)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Class cls = target.getClass();

        Map<String, Method> methodMap;
        if (!methodCache.containsKey(cls.getName())) {
            methodMap = new HashMap<>();
            methodCache.put(cls.getName(), methodMap);

            Method[] methods = cls.getMethods();
            for (Method m : methods) {
                methodMap.put(m.getName(), m);
            }
        } else {
            methodMap = methodCache.get(cls.getName());
        }

        ArrayList<String> usedProperties = new ArrayList<>();

        for (HashMap.Entry<String, Object> entry : source.entrySet()) {
            String prop = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }

            String setterName = "set" + prop.substring(0, 1).toUpperCase() + prop.substring(1);

            if (methodMap.containsKey(setterName)) {
                Method setter = methodMap.get(setterName);

                Class paramType = setter.getParameterTypes()[0];
                if (setterName.contains("Color")) {
                    value = Color.parseColor((String) value);
                } else if (paramType == int.class) {
                    value = ((Double)value).intValue();
                } else if (paramType == float.class) {
                    value = ((Double)value).floatValue();
                } else if (paramType.isEnum()) {
                    value = Enum.valueOf(paramType, (String) value);
                }

                setter.invoke(target, value);
                usedProperties.add(prop);
            }
        }

        return usedProperties.toArray(new String[0]);
    }
}
