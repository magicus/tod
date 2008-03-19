package foo;

public class Foo {
	public void f(Superclass o) {
		System.out.println("Foo.f()");
	}
	
	public static void main(String[] args) {
		Foo myFoo = new Foo();
		myFoo.f(getParam());
	}
	
	private static Superclass getParam() {
		return new Subclass();
	}
}
