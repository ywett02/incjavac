package testData.src;

public class TestClass extends AbstractClassImpInterface {

    @Override
    void abstractClassMethod() {
    }

    @Override
    public void interfaceMethod() {
    }

    public void callDependentMethod() {
        new IndependentClass().method();
    }

    public void printConstantValue() {
        System.out.println(ConstantOwner.CONSTANT_VALUE);
    }
}
