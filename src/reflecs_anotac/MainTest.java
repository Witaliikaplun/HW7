package reflecs_anotac;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainTest {

    public static void main(String[] args) {
        TestingClass.start(TestingClass.class);
    }
}

class TestingClass {

    public static void start(Class testClass) {
        performTests(testClass);
    }

    private static void performTests(Class ccc) throws RuntimeException {
        TestingClass objTest = null;
        try {
            objTest = (TestingClass)ccc.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Method befMet = null;
        Method aftMet = null;
        Method[] methods = ccc.getMethods();    //получаем массив методов
        List<MethPrior> testMethods = new ArrayList<>();

        for (Method m : methods)
            if (m.getAnnotation(BeforeSuite.class) != null) {
                if (befMet != null) throw new RuntimeException("Метод с аннотацией @BeforeSuite должен быть один");
                befMet = m;
            } else if (m.getAnnotation(AfterSuite.class) != null) {
                if (aftMet != null) throw new RuntimeException("Метод с аннотацией @AfterSuite должен быть один");
                aftMet = m;
            } else if (m.getAnnotation(Test.class) != null){
                Test annotationTst = m.getAnnotation(Test.class);
                testMethods.add(new MethPrior(m, annotationTst.value()));
            }
        //отсортируем коллекцию методов по приоритетам
        testMethods.sort(Comparator.comparing(MethPrior::getPriority));

        try {
            if (befMet != null)
                befMet.invoke(objTest);

            for (MethPrior m : testMethods)
                m.method.invoke(objTest);

            if (aftMet != null)
                aftMet.invoke(objTest);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    //методы с приоритетами приоритетов
    @Test(value = 8)
    public void Test5() {
        System.out.println("Тест 8");
    }
    @Test(value = 4)
    public void Test2(){
        System.out.println("Тест 4");
    }
    @Test(value = 1)
    public void Test1(){
        System.out.println("Тест 1");
    }



    @BeforeSuite
    public void BeforeAll(){
        System.out.println("Предварительный метод");
    }
    @AfterSuite
    public void AfterAll() {
        System.out.println("Крайний метод");
    }
}

class MethPrior {
    public Method method;
    public Integer priority;

    public MethPrior(Method method, int priority) {
        this.method = method;
        this.priority = priority;
    }

    public Integer getPriority() {
        return priority;
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface BeforeSuite {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface AfterSuite {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Test {
    int value();
}