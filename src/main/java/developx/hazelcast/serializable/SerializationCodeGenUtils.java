package developx.hazelcast.serializable;

public final class SerializationCodeGenUtils {

    private SerializationCodeGenUtils(){
        throw new IllegalStateException("생성할 수 없는 클래스입니다.");
    }

    public static String getWriteMethod(String type, String name) {
        return switch (type) {
            case "int", "java.lang.Integer" -> "writeInt(" + name + ")";
            case "long", "java.lang.Long" -> "writeLong(" + name + ")";
            case "double", "java.lang.Double" -> "writeDouble(" + name + ")";
            case "boolean", "java.lang.Boolean" -> "writeBoolean(" + name + ")";
            case "java.lang.String" -> "writeUTF(" + name + ")";
            default -> "writeObject(" + name + ")";
        };
    }

    public static String getReadExpression(String type) {
        return switch (type) {
            case "int", "java.lang.Integer" -> "in.readInt()";
            case "long", "java.lang.Long" -> "in.readLong()";
            case "double", "java.lang.Double" -> "in.readDouble()";
            case "boolean", "java.lang.Boolean" -> "in.readBoolean()";
            case "java.lang.String" -> "in.readUTF()";
            default -> "(" + type + ") in.readObject()";
        };
    }
}