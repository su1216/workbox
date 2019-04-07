package com.su.compiler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.auto.service.AutoService;
import com.su.annotations.NoteComponent;
import com.su.annotations.NoteJsCallAndroid;
import com.su.annotations.NoteJsFilepath;
import com.su.annotations.NoteJsFunction;
import com.su.annotations.NoteWebView;
import com.su.compiler.entity.NoteComponentEntity;
import com.su.compiler.entity.NoteJsCallAndroidEntity;
import com.su.compiler.entity.NoteJsFilepathEntity;
import com.su.compiler.entity.NoteJsFunctionEntity;
import com.su.compiler.entity.NoteWebViewEntity;
import com.su.compiler.entity.Parameter;
import com.su.compiler.entity.SimpleParameter;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

@SupportedOptions("MODULE_NAME")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class NoteProcessor extends AbstractProcessor {

    static final String WORKING_DIR = System.getProperty("user.dir");
    private static String GENERATED_DIR_PATH;
    private Messager mMessager;
    private String buildType;
    private static int sCount;
    private static boolean sDone;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        Map<String, String> options = processingEnv.getOptions();
        buildType = options.get("buildType");
        System.out.println("buildType: " + buildType);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        mMessager = processingEnv.getMessager();
        Map<String, String> options =  processingEnv.getOptions();
        String moduleName = options.get("MODULE_NAME");
        note(mMessager, "moduleName: " + moduleName);
        prepareGeneratedDirPath(moduleName);
        if (roundEnvironment.processingOver()) {
            if (sDone) {
                return true;
            }
            sDone = true;
            note(mMessager, "processed!");
        } else {
            if (sCount == 1) {
                return true;
            }
            sCount++;
            note(mMessager, "start processing...");
            note(mMessager, "WORKING_DIR: " + WORKING_DIR);
            processNoteJsCallAndroid(roundEnvironment);
            processNoteWebView(roundEnvironment);
            processNoteComponent(roundEnvironment);
            processNoteJsFunction(roundEnvironment);
        }
        return true;
    }

    private void prepareGeneratedDirPath(String moduleName) {
        GENERATED_DIR_PATH = WORKING_DIR + "/" + moduleName + "/src/main/assets/generated/";
        File file = new File(GENERATED_DIR_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private void processNoteJsCallAndroid(RoundEnvironment roundEnvironment) {
        Map<String, List<NoteJsCallAndroidEntity>> allResults = new HashMap<>();
        Collection<? extends Element> annotatedElements = roundEnvironment.getElementsAnnotatedWith(NoteJsCallAndroid.class);
        for (Element e : annotatedElements) {
            String className = e.getEnclosingElement().getSimpleName().toString();
            List<NoteJsCallAndroidEntity> results = allResults.computeIfAbsent(className, s -> new ArrayList<>());
            NoteJsCallAndroid noteJsCallAndroid = e.getAnnotation(NoteJsCallAndroid.class);
            NoteJsCallAndroidEntity entity = new NoteJsCallAndroidEntity();
            entity.setDescription(noteJsCallAndroid.description());
            entity.setParameters(noteJsCallAndroid.parameters());
            entity.setFunctionName(e.getSimpleName().toString());

            results.add(entity);
            note(mMessager, "entity: " + entity);
        }

        Set<String> keySet = allResults.keySet();
        for (String key : keySet) {
            List<NoteJsCallAndroidEntity> results = allResults.get(key);
            String jsonString = JSON.toJSONString(results, true);
            String filename = "JsCallAndroid-" + key;
            note(mMessager, filename + ": " + jsonString);
            save(filename, jsonString);
        }
    }

    private void processNoteWebView(RoundEnvironment roundEnvironment) {
        List<NoteWebViewEntity> list = new ArrayList<>();
        Set<? extends Element> annotatedElements = roundEnvironment.getElementsAnnotatedWith(NoteWebView.class);
        Set<VariableElement> fields = ElementFilter.fieldsIn(annotatedElements);
        for (VariableElement e : fields) {
            NoteWebView noteWebView = e.getAnnotation(NoteWebView.class);
            NoteWebViewEntity entity = new NoteWebViewEntity();
            entity.setDescription(noteWebView.description());
            entity.setMethod(noteWebView.method());
            entity.setRequestHeaders(makeParameters(noteWebView.requestHeaders()));
            entity.setParameters(makeParameters(noteWebView.parameters()));
            entity.setUrl(e.getConstantValue().toString());
            entity.setNeedLogin(noteWebView.needLogin());
            entity.setTitle(noteWebView.title());
            entity.setPostContent(noteWebView.postContent());
            list.add(entity);
            note(mMessager, "entity: " + entity);
        }

        String jsonString = JSON.toJSONString(list, true);
        note(mMessager, "webView: " + jsonString);
        save("webView", jsonString);
    }

    private String processUrl(@Nonnull String urlString) {
        try {
            URL url = new URL(urlString);
            if (url.getQuery() == null) {
                return urlString;
            }
            Map<String, String> queries = splitQuery(url);
            Set<String> set = queries.keySet();
            List<String> queryList = new ArrayList<>(set);
            Collections.sort(queryList);
            StringBuilder stringBuilder = new StringBuilder(urlString.replaceAll(url.getQuery() + "$", ""));
            for (String key : queryList) {
                stringBuilder.append(key);
                stringBuilder.append("=");
                stringBuilder.append(queries.get(key));
                stringBuilder.append("&");
            }
            if (!queryList.isEmpty()) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
            return stringBuilder.toString();
        } catch (MalformedURLException e) {
            error(mMessager, e.getMessage());
        }
        return urlString;
    }

    private static Map<String, String> splitQuery(URL url) {
        Map<String, String> queryPairs = new LinkedHashMap<>();
        String query = url.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            queryPairs.put(pair.substring(0, idx), pair.substring(idx + 1));
        }
        return queryPairs;
    }

    //request header/response header/requestBody 等需要key-value形式，解析展示时需要去掉最外层包裹
    private String toKeyValueJSONString(String[] keyValues) {
        JSONObject jsonObject = new JSONObject();
        if (keyValues == null || keyValues.length == 0) {
            return "";
        }
        for (String parameter : keyValues) {
            int index = parameter.indexOf("=");
            jsonObject.put(parameter.substring(0, index), parameter.substring(index + 1, parameter.length()));
        }
        return JSON.toJSONString(jsonObject, true);
    }

    private void processNoteComponent(RoundEnvironment roundEnvironment) {
        List<NoteComponentEntity> list = new ArrayList<>();
        Set<? extends Element> annotatedElements = roundEnvironment.getElementsAnnotatedWith(NoteComponent.class);
        Set<TypeElement> types = ElementFilter.typesIn(annotatedElements);
        for (TypeElement e : types) {
            NoteComponent noteComponent = e.getAnnotation(NoteComponent.class);
            NoteComponentEntity entity = new NoteComponentEntity();
            entity.setDescription(noteComponent.description());
            entity.setClassName(e.getQualifiedName().toString());
            entity.setAction(noteComponent.action());
            entity.setFlags(noteComponent.flags());
            entity.setBuildType(noteComponent.buildType());
            entity.setType(noteComponent.type());
            com.su.annotations.Parameter[] parameters = noteComponent.parameters();
            int length = parameters.length;
            Parameter[] realParameters = new Parameter[length];
            for (int i = 0; i < length; i++) {
                com.su.annotations.Parameter parameter = parameters[i];
                Parameter parameterEntity = new Parameter();
                parameterEntity.setParameter(parameter.parameter());
                parameterEntity.setParameterName(parameter.parameterName());
                try {
                    parameter.parameterClass();
                } catch (MirroredTypeException mte) {
                    TypeMirror typeMirror = mte.getTypeMirror();
                    parameterEntity.setParameterClassName(typeMirror.toString());
                }
                parameterEntity.setParameterRequired(parameter.parameterRequired());
                realParameters[i] = parameterEntity;
            }
            entity.setParameters(realParameters);

            list.add(entity);
            note(mMessager, "entity: " + entity);
        }

        String jsonString = JSON.toJSONString(list, true);
        note(mMessager, "components: " + jsonString);
        save("components", jsonString);
    }

    private void processNoteJsFunction(RoundEnvironment roundEnvironment) {
        List<NoteJsFunctionEntity> list = new ArrayList<>();
        Set<? extends Element> annotatedElements = roundEnvironment.getElementsAnnotatedWith(NoteJsFunction.class);
        Set<VariableElement> fields = ElementFilter.fieldsIn(annotatedElements);
        //获取有此注解的元素
        for (VariableElement element : fields) {
            NoteJsFunction noteJsFunction = element.getAnnotation(NoteJsFunction.class);
            NoteJsFunctionEntity entity = new NoteJsFunctionEntity();
            entity.setName(element.getConstantValue().toString());
            entity.setJsFilepath(new NoteJsFilepathEntity(noteJsFunction.jsFilepath()));
            entity.setDescription(noteJsFunction.description());
            com.su.annotations.Parameter[] parameters = noteJsFunction.parameters();
            int length = parameters.length;
            Parameter[] realParameters = new Parameter[length];
            for (int i = 0; i < length; i++) {
                com.su.annotations.Parameter parameter = parameters[i];
                Parameter parameterEntity = new Parameter();
                parameterEntity.setParameter(parameter.parameter());
                parameterEntity.setParameterName(parameter.parameterName());
                try {
                    parameter.parameterClass();
                } catch (MirroredTypeException mte) {
                    TypeMirror typeMirror = mte.getTypeMirror();
                    parameterEntity.setParameterClassName(typeMirror.toString());
                }
                parameterEntity.setParameterRequired(parameter.parameterRequired());
                realParameters[i] = parameterEntity;
            }
            entity.setParameters(realParameters);
            //获取此元素上的所有注解，并找到目标注解
            AnnotationMirror annotationMirror = getAnnotationMirror(element, NoteJsFunction.class);
            //获取注解内容
            Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotationMirror.getElementValues();
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
                String key = entry.getKey().getSimpleName().toString();
                Object value = entry.getValue().getValue();
                switch (key) {
                    case "resultClass":
                        TypeMirror typeMirror = (TypeMirror) value;
                        entity.setResultClassName(typeMirror.toString());
                        break;
                    default:
                        break;
                }
            }
            list.add(entity);
            note(mMessager, "entity: " + entity);
        }

        String jsonString = JSON.toJSONString(list, true);
        note(mMessager, "js: " + jsonString);
        save("js", jsonString);
    }

    private AnnotationMirror getAnnotationMirror(Element typeElement, Class<?> clazz) {
        String clazzName = clazz.getName();
        for (AnnotationMirror m : typeElement.getAnnotationMirrors()) {
            if (m.getAnnotationType().toString().equals(clazzName)) {
                return m;
            }
        }
        return null;
    }

    private ArrayList<SimpleParameter> makeParameters(String[] parameters) {
        ArrayList<SimpleParameter> list = new ArrayList<>();
        if (parameters != null) {
            for (String parameter : parameters) {
                int index = parameter.indexOf("=");
                list.add(new SimpleParameter(parameter.substring(0, index), parameter.substring(index + 1, parameter.length())));
            }
        }
        return list;
    }

    private void save(String filename, String result) {
        try {
            List<String> lines = Arrays.asList(result);
            Path path = Paths.get(GENERATED_DIR_PATH + filename + ".json");
            note(mMessager, "PATH: " + path.toAbsolutePath());
            Files.write(path, lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            error(mMessager, e.getMessage());
        }
    }

    private static void note(Messager messager, CharSequence note) {
        messager.printMessage(Diagnostic.Kind.NOTE, note);
    }

    private static void error(Messager messager, CharSequence error) {
        messager.printMessage(Diagnostic.Kind.ERROR, error);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        annotations.add(NoteJsCallAndroid.class);
        annotations.add(NoteComponent.class);
        annotations.add(NoteJsFunction.class);
        annotations.add(NoteJsFilepath.class);
        annotations.add(NoteWebView.class);
        annotations.add(com.su.annotations.Parameter.class);
        return annotations;
    }
}
