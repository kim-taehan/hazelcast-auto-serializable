# 🔄 AutoSerializableProcessor

**Hazelcast IdentifiedDataSerializable 자동 생성용 어노테이션 프로세서**

`@AutoDataSerializable` 어노테이션을 이용해 Java 클래스에 최소한의 정보를 제공하면, Hazelcast의 고성능 직렬화 인터페이스인 `IdentifiedDataSerializable` 구현체 및 Factory 클래스를 자동으로 생성해주는 Annotation Processor입니다.

---

## ✨ 특징

- `IdentifiedDataSerializable` 구현 자동 생성
- Hazelcast `DataSerializableFactory` 클래스 자동 생성
- primitive 및 boxed 타입 모두 지원
- 클래스 필드 기반 직렬화/역직렬화 로직 자동 생성
- `switch` 기반 Factory 로 빠른 인스턴스 생성 성능

---

## 🛠 사용 예시

### Annotated 클래스 정의
```java
@AutoSerializable(factoryId = 2, classId = 41)
public class SampleVo2 {
    private int age;
    private String name;
}
```

### 컴파일 시 자동 생성 
#### SampleVo2Impl.java (자동 생성)
```java
public class SampleVo2Impl extends SampleVo2 implements IdentifiedDataSerializable {
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(age);
        out.writeUTF(name);
    }

    public void readData(ObjectDataInput in) throws IOException {
        this.age = in.readInt();
        this.name = in.readUTF();
    }

    public int getFactoryId() { return 2; }
    public int getClassId() { return 41; }
}
```
#### FactoryId2SerializableFactory.java (자동 생성)
```java
public class FactoryId2SerializableFactory implements DataSerializableFactory {
    public IdentifiedDataSerializable create(int classId) {
        return switch (classId) {
            case 41 -> new SampleVo2Impl();
            default -> null;
        };
    }
}
```

---

### 📄 내부 구성 클래스
#### AutoDataSerializable annotation 
```java
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface AutoDataSerializable {
    int factoryId();
    int classId();
}
```

#### AutoSerializableProcessor
- 어노테이션을 분석하고 Impl 클래스와 Factory 클래스를 생성
- @AutoService(Processor.class)로 등록

#### SerializationCodeGenUtils
- 타입별 직렬화/역직렬화 메서드 매핑 도우미
- primitive + boxed 타입 자동 대응

#### FactoryClassGenerator
- 각 factoryId 별로 생성된 클래스들을 모아 DataSerializableFactory 구현체를 생성

#### ImplClassGenerator
- 클래스의 필드 정보를 기반으로 writeData / readData 메서드 구현
- getFactoryId(), getClassId() 메서드를 어노테이션 정보로부터 자동 생성

---

### 💡 장단점 
#### 장점
- 코드 반복 제거 (writeData/readData 수동 구현 불필요)
- factoryId/classId 체계적인 관리
- Hazelcast IdentifiedDataSerializable 자동화

#### 향후 개선
- 상속 필드 포함 처리
- 사용자 정의 타입 직렬화 지원 (LocalDate, Enum, 등)
- 직렬화 순서 지정 기능
- Lombok과의 통합 지원

---


### 📌 적용방법

#### maven
- nexus or local nexus 등록 후 사용 
```xml
<dependencies>
    <dependency>
        <groupId>com.hazelcast</groupId>
        <artifactId>hazelcast</artifactId>
        <version>5.3.4</version>
    </dependency>
    <dependency>
        <groupId>developx</groupId>
        <artifactId>auto-serializable-processor</artifactId>
        <version>1.0.0</version>
        <scope>provided</scope> 
    </dependency>
</dependencies>

<build>
<plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.11.0</version>
        <configuration>
            <annotationProcessorPaths>
                <path>
                    <groupId>developx</groupId>
                    <artifactId>auto-serializable-processor</artifactId>
                    <version>1.0.0</version>
                </path>
            </annotationProcessorPaths>
        </configuration>
    </plugin>
</plugins>
</build>


```

#### gradle
```groovy

dependencies {
    implementation 'com.hazelcast:hazelcast:5.3.4'

    compileOnly 'developx:auto-serializable-processor:1.0.0'
    annotationProcessor 'developx:auto-serializable-processor:1.0.0'
}
```

```kotlin
dependencies {
    annotationProcessor("com.yourcompany:auto-serializable-processor:1.0.0")
}

### 📄 라이선스
- MIT License © 2025 Kim Taehan (developx)