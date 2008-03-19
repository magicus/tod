package foo;

public aspect MyAspect {
	before(Subclass c): call(* Foo.f(Superclass))
	   && args(c) {
		System.out.println("MyAspect.before()");
	};
}
