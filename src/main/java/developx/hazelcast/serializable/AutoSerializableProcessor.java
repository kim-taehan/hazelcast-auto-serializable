package developx.hazelcast.serializable;

import com.google.auto.service.AutoService;
import developx.hazelcast.serializable.generator.FactoryClassGenerator;
import developx.hazelcast.serializable.generator.ImplClassGenerator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.*;

@AutoService(Processor.class)
@SupportedAnnotationTypes("developx.hazelcast.serializable.AutoDataSerializable")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class AutoSerializableProcessor extends AbstractProcessor {

    private final Map<Integer, List<String>> factoryGroups = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();

        for (Element element : roundEnv.getElementsAnnotatedWith(AutoDataSerializable.class)) {
            if (element.getKind() != ElementKind.CLASS) continue;

            TypeElement typeElement = (TypeElement) element;
            AutoDataSerializable ann = typeElement.getAnnotation(AutoDataSerializable.class);

            try {
                ImplClassGenerator.generateImplClass(processingEnv, typeElement, ann);
                String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
                String implClassName = typeElement.getSimpleName() + "Impl";
                factoryGroups.computeIfAbsent(ann.factoryId(), k -> new ArrayList<>())
                        .add(packageName + "." + implClassName + ":" + ann.classId() + ":" + ann.factoryId());
            } catch (Exception e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Error: " + e.getMessage());
            }
        }

        if (roundEnv.processingOver() && !factoryGroups.isEmpty()) {
            factoryGroups.forEach((factoryId, classInfos) -> {
                FactoryClassGenerator.generateFactoryClass(processingEnv, factoryId, classInfos);
            });
        }

        return true;
    }
}
