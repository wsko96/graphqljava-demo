package com.example.graphqljavademo.bookdetails;

import java.util.Objects;

public class Author {

    private final String id;
    private final String firstName;
    private final String lastName;

    public Author(final String id, final String firstName, final String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Author)) {
            return false;
        }

        final Author that = (Author) o;

        return this.id == that.id
                && Objects.equals(this.firstName, that.firstName)
                && Objects.equals(this.lastName, that.lastName);
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(id).hashCode();
    }

    @Override
    public String toString() {
        return "[" + id + "] " + firstName + " " + lastName;
    }
}
