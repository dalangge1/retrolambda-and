// Copyright © 2013-2016 Esko Luontola and other Retrolambda contributors
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.retrolambda.test;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static net.orfjackal.retrolambda.test.TestUtil.assertClassExists;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertNotEquals;

public class LambdaClassesTest {

    @Test
    public void the_sequence_number_starts_from_1_for_each_enclosing_class() {
        assertClassExists(Dummy1.class.getName() + "$$Lambda$1");
        assertClassExists(Dummy1.class.getName() + "$$Lambda$2");
        assertClassExists(Dummy2.class.getName() + "$$Lambda$1");
        assertClassExists(Dummy2.class.getName() + "$$Lambda$2");
    }

    @SuppressWarnings("UnusedDeclaration")
    private class Dummy1 {
        private Dummy1() {
            Runnable lambda1 = () -> {
            };
            Runnable lambda2 = () -> {
            };
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    private class Dummy2 {
        private Dummy2() {
            Runnable lambda1 = () -> {
            };
            Runnable lambda2 = () -> {
            };
        }
    }


    @Test
    public void capturing_lambda_classes_contain_no_unnecessary_methods() throws ClassNotFoundException {
        assertThat(getMethodsNames(findLambdaClass(Capturing.class)),
                is(ImmutableSet.of("lambdaFactory$", "run")));
    }

    @SuppressWarnings("UnusedDeclaration")
    private class Capturing {
        private Capturing() {
            Runnable lambda = () -> System.out.println(hashCode());
        }
    }


    @Test
    public void non_capturing_lambda_classes_contain_no_unnecessary_methods() throws ClassNotFoundException {
        assertThat(getMethodsNames(findLambdaClass(NonCapturing.class)),
                is(ImmutableSet.of("lambdaFactory$", "run")));
    }

    @SuppressWarnings("UnusedDeclaration")
    private class NonCapturing {
        private NonCapturing() {
            Runnable lambda = () -> {
            };
        }
    }


    @Test
    public void enclosing_classes_contain_no_unnecessary_methods_in_addition_to_the_lambda_body() throws ClassNotFoundException {
        assertThat("non-capturing lambda", getMethodsNames(NonCapturing.class), contains(startsWith("lambda$new$")));
        assertThat("capturing lambda", getMethodsNames(Capturing.class), contains(startsWith("lambda$new$")));
    }

    private class Parent {
        protected void foo() {
            Runnable lambda = () -> {
                System.out.println("parent");
            };
        }
    }

    private class Child extends Parent {
        @Override
        protected void foo() {
            super.foo();
            Runnable lambda = () -> {
                System.out.println("child");
            };
        }
    }

    @Test
    public void child_class_lambda_doesnt_hide_parent_class_lambda() throws ClassNotFoundException {
        Set<String> parentLambdas = getLambdaMethodNames(Parent.class);
        Set<String> childLambdas = getLambdaMethodNames(Child.class);
        assertNotEquals("child lambda hides parent lambda", parentLambdas, childLambdas);
    }


    // helpers

    private static Class<?> findLambdaClass(Class<?> clazz) throws ClassNotFoundException {
        return Class.forName(clazz.getName() + "$$Lambda$1");
    }

    private static Set<String> getLambdaMethodNames(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        Set<String> uniqueNames = new HashSet<>();
        for (Method method : methods) {
            if (method.getName().startsWith("lambda$")) {
                uniqueNames.add(method.getName());
            }
        }
        return uniqueNames;
    }

    private static Set<String> getMethodsNames(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        Set<String> uniqueNames = new HashSet<>();
        for (Method method : methods) {
            uniqueNames.add(method.getName());
        }
        assertThat("unexpected overloaded methods", methods, arrayWithSize(uniqueNames.size()));
        return uniqueNames;
    }
}
