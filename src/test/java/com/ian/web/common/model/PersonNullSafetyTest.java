package com.ian.web.common.model;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class PersonNullSafetyTest {

    @Test
    void getAgeReturnsNullWhenBirthdateIsMissing() {
        Person person = new Person();

        assertNull(person.getAge());
    }
}
