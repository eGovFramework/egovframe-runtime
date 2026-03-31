package org.egovframe.rte.fdl.property.annotation;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.ObjectUtils;

/**
 * Simple test bean used for testing bean factories, the AOP framework etc.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 15 April 2001
 */
public class TestBean {

    private String country;

    private BeanFactory beanFactory;

    private boolean postProcessed;

    private String name;

    private String sex;

    private int age;

    private boolean destroyed;

    public TestBean(String name) {
        this.name = name;
    }

    public TestBean(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public boolean isPostProcessed() {
        return postProcessed;
    }

    public void setPostProcessed(boolean postProcessed) {
        this.postProcessed = postProcessed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
        if (this.name == null) {
            this.name = sex;
        }
    }

    public void destroy() {
        this.destroyed = true;
    }

    public boolean wasDestroyed() {
        return destroyed;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || !(other instanceof TestBean)) {
            return false;
        }

        TestBean tb2 = (TestBean) other;

        return (ObjectUtils.nullSafeEquals(this.name, tb2.name) && this.age == tb2.age);
    }

    public int hashCode() {
        return this.age;
    }

    public String toString() {
        return this.name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

}
