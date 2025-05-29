package developx.hazelcast.serializable.generator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FactoryClassGenerator {

    public static void generateFactoryClass(ProcessingEnvironment processingEnv, int factoryId, List<String> classInfos) {
        try {
            String firstFqcn = classInfos.getFirst().split(":")[0];
            String packageName = firstFqcn.substring(0, firstFqcn.lastIndexOf('.'));
            String factoryClassName = "FactoryId" + factoryId + "SerializableFactory";

            JavaFileObject factoryFile = processingEnv.getFiler().createSourceFile(packageName + "." + factoryClassName);
            try (Writer writer = new OutputStreamWriter(factoryFile.openOutputStream(), StandardCharsets.UTF_8)) {
                writer.write("package " + packageName + ";\n\n");
                writer.write("import com.hazelcast.nio.serialization.DataSerializableFactory;\n");
                writer.write("import com.hazelcast.nio.serialization.IdentifiedDataSerializable;\n\n");

                // import unique fqcn
                Set<String> imports = new HashSet<>();
                for (String info : classInfos) {
                    String fqcn = info.split(":")[0];
                    imports.add("import " + fqcn + ";\n");
                }
                for (String imp : imports) writer.write(imp);

                writer.write("\npublic class " + factoryClassName + " implements DataSerializableFactory {\n\n");

                writer.write("    @Override\n");
                writer.write("    public IdentifiedDataSerializable create(int classId) {\n");
                writer.write("        return switch (classId) {\n");

                for (String info : classInfos) {
                    String[] parts = info.split(":");
                    String fqcn = parts[0];
                    String classId = parts[1];
                    writer.write("            case " + classId + " -> new " + fqcn.substring(fqcn.lastIndexOf('.') + 1) + "();\n");
                }

                writer.write("            default -> null;\n");
                writer.write("        };\n");
                writer.write("    }\n");

                writer.write("}\n");
            }
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Factory gen error: " + e.getMessage());
        }
    }
}
