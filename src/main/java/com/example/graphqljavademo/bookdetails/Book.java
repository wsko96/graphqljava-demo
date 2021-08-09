package com.example.graphqljavademo.bookdetails;

import java.util.Objects;

public class Book {

    private final String id;
    private final String title;
    private final int pageCount;
    private final String authorId;

    public Book(final String id, final String title, final int pageCount, final String authorId) {
        this.id = id;
        this.title = title;
        this.pageCount = pageCount;
        this.authorId = authorId;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getPageCount() {
        return pageCount;
    }

    public String getAuthorId() {
        return authorId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Book)) {
            return false;
        }

        final Book that = (Book) o;

        return this.id == that.id
                && Objects.equals(this.title, that.title)
                && this.pageCount == that.pageCount
                && this.authorId == that.authorId;
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(id).hashCode();
    }

    @Override
    public String toString() {
        return "[" + id + "] " + title + " (" + pageCount + ") by " + authorId;
    }
}
