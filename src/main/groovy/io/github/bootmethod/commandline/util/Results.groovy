package io.github.bootmethod.commandline.util

import groovy.json.DefaultJsonGenerator
import groovy.json.JsonGenerator
import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import io.github.bootmethod.commandline.Result
import org.apache.groovy.json.internal.CharBuf
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Modifier

/**
 * 
 * @
 *  
 * */

@CompileStatic
class Results {
    private static Logger LOG = LoggerFactory.getLogger(Results.class)

    static class MyJsonGenerator extends DefaultJsonGenerator {

        Stack<Object> stack = new Stack<>()

        MyJsonGenerator() {
            super(new JsonGenerator.Options())
        }

        @Override
        protected boolean shouldExcludeType(Class<?> type) {
            if (Closure.class.isAssignableFrom(type)) {
                return true
            }
            return super.shouldExcludeType(type)
        }

        @Override
        protected Map<?, ?> getObjectProperties(Object object) {
            def properties = getObjectProperties(object, object.getClass(), [:] as Map<?, ?>)
            properties.remove("class");
            properties.remove("declaringClass");
            properties.remove("metaClass");
            return properties
        }

        Map<?, ?> getObjectProperties(Object object, Class cls, Map<?, ?> map) {
            if (cls == Object.class) {
                return map
            }

            Class superCls = cls.getSuperclass()

            getObjectProperties(object, superCls, map)

            cls.getDeclaredFields().each {
                if (Modifier.isStatic(it.getModifiers())) {
                    return
                }
                if (!it.isAccessible()) {

                    it.setAccessible(true)
                }
                Object value = it.get(object)
                map.put(it.getName(), value)
            }

            return map

        }

        @Override
        protected void writeObject(String key, Object object, CharBuf buffer) {
            LOG.info("writeObject,key:${key},obj:${object}")
            int maxDeep = 100
            if (stack.size() > maxDeep && object) {
                object = "...skip-print.because.stack-depth-exceed...${maxDeep}...object.class...${object.getClass().getName()}..."
            }
            stack.push(object)
            try {
                super.writeObject(key, object, buffer)
            } finally {
                stack.pop()
            }
        }

    }


    static Result SILENCE = new Result() {
        @Override
        void print(PrintStream context) {
        }
    }

    static class AsJsonResult implements Result {
        Object result

        @Override
        void print(PrintStream context) {
            String json = new MyJsonGenerator().toJson(result)
            context.println(JsonOutput.prettyPrint(json))
        }
    }

    static Result asResult(Object result) {
        if (result == null) {
            return SILENCE
        } else if (result instanceof Result) {
            return (Result) result
        } else if (result instanceof Throwable) {
            return new ExceptionResult((Throwable) result)
        } else {
            return new AsJsonResult(result: result)
        }
    }

}
