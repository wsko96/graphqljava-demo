package com.example.graphqljavademo.bookdetails;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;

@Component
public class GraphQLDataFetchers {

    private static List<Book> books = new LinkedList<>(Arrays.asList(
            new Book("book-1", "Harry Potter and the Philosopher's Stone", 223, "author-1"),
            new Book("book-2", "Moby Dick", 635, "author-2"),
            new Book("book-3", "Interview with the vampire", 371, "author-3")
            ));

    private static List<Author> authors = new LinkedList<>(Arrays.asList(
            new Author("author-1", "Joanne", "Rowling"),
            new Author("author-2", "Herman", "Melville"),
            new Author("author-3", "Anne", "Rice")
            ));

    public DataFetcher<List<Book>> getBooksDataFetcher() {
        return env -> books;
    }

    public DataFetcher<Book> getBookByIdDataFetcher() {
        return env -> {
            final String id = env.getArgument("id");
            return books
                    .stream()
                    .filter(book -> Objects.equals(book.getId(), id))
                    .findFirst()
                    .orElse(null);
        };
    }

    public DataFetcher<List<Author>> getAuthorsDataFetcher() {
        return env -> authors;
    }

    public DataFetcher<Author> getAuthorDataFetcher() {
        return env -> {
            final Book book = env.getSource();
            final String authorId = book.getAuthorId();
            return authors
                    .stream()
                    .filter(author -> Objects.equals(author.getId(), authorId))
                    .findFirst()
                    .orElse(null);
        };
    }

    public DataFetcher<Author> getCreateAuthorFetcher() {
        return env -> {
            final String id = "author-" + (authors.size() + 1);
            final String firstName = env.getArgument("firstName");
            final String lastName = env.getArgument("lastName");
            final Author author = new Author(id, firstName, lastName);
            authors.add(author);
            return author;
        };
    }
}
