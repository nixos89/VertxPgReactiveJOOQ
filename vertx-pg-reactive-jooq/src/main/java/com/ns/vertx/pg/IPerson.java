package com.ns.vertx.pg;

@FunctionalInterface
public interface IPerson {

	public void test(String value); // it MUST have method like this 1 -> ONLY DECLARED!
	
	public default void doSomething() {}

	public default void doSomething(String str) { System.out.println(str); } // overloading ALLOWED!

	public static int personsFirstNameLength(String firstName) {
		return firstName.length();
	}

	public static int personsLastNameLength(String lastName) {
		return lastName.length();
	}
}
