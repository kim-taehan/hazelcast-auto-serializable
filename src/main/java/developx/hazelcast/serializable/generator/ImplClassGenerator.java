package developx.hazelcast.serializable.generator;

import developx.hazelcast.serializable.AutoDataSerializable;
import developx.hazelcast.serializable.SerializationCodeGenUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaFileObject;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ImplClassGenerator {

    public static void generateImplClass(ProcessingEnvironment processingEnv, TypeElement typeElement, AutoDataSerializable ann) throws Exception {
        String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
        String className = typeElement.getSimpleName().toString();
        String implClassName = className + "Impl";
        int factoryId = ann.factoryId();
        int classId = ann.classId();

        List<VariableElement> fields = ElementFilter.fieldsIn(typeElement.getEnclosedElements());

        JavaFileObject implFile = processingEnv.getFiler().createSourceFile(packageName + "." + implClassName);
        try (Writer writer = new OutputStreamWriter(implFile.openOutputStream(), StandardCharsets.UTF_8)) {
            writer.write("package " + packageName + ";\n\n");
            writer.write("import com.hazelcast.nio.ObjectDataInput;\n");
            writer.write("import com.hazelcast.nio.ObjectDataOutput;\n");
            writer.write("import com.hazelcast.nio.serialization.IdentifiedDataSerializable;\n");
            writer.write("import java.io.IOException;\n\n");

            writer.write("public class " + implClassName + " extends " + className + " implements IdentifiedDataSerializable {\n");

            writer.write("    @Override\n");
            writer.write("    public void writeData(ObjectDataOutput out) throws IOException {\n");
            for (VariableElement field : fields) {
                String name = field.getSimpleName().toString();
                String type = field.asType().toString();
                writer.write("        out." + SerializationCodeGenUtils.getWriteMethod(type, name) + ";\n");
            }
            writer.write("    }\n\n");

            writer.write("    @Override\n");
            writer.write("    public void readData(ObjectDataInput in) throws IOException {\n");
            for (VariableElement field : fields) {
                String name = field.getSimpleName().toString();
                String type = field.asType().toString();
                writer.write("        this." + name + " = " + SerializationCodeGenUtils.getReadExpression(type) + ";\n");
            }
            writer.write("    }\n\n");

            writer.write("    @Override public int getFactoryId() { return " + factoryId + "; }\n");
            writer.write("    @Override public int getClassId() { return " + classId + "; }\n");

            writer.write("}\n");
        }
    }
}
