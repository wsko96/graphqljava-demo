# graphqljava-demo

## Lab 1

- "Spring Initializr" 사이트에서 ( https://start.spring.io/ ) 아래 예와 같은 옵션으로 Spring Boot Application 생성

```
groupId: com.example
artifactId: graphqljava-demo
name: graphqljava-demo
Packaging: War
```

- `pom.xml`에 아래 Dependency들을 추가

```
    <dependency>
      <groupId>com.graphql-java</groupId>
      <artifactId>graphql-java</artifactId>
      <version>17.0</version>
    </dependency>
    <dependency>
      <groupId>com.graphql-java</groupId>
      <artifactId>graphql-java-spring-boot-starter-webmvc</artifactId>
      <version>2.0</version>
    </dependency>
    <dependency>
      <groupId>com.google.guava</groupId>
      <artifactId>guava</artifactId>
      <version>30.1.1-jre</version>
    </dependency>
```

- src/main/resources/schema.graphqls 파일 추가

```
type Query {
  books: [Book]
  bookById(id: ID): Book
  authors: [Author]
}

type Mutation {
  createAuthor(firstName: String, lastName: String): Author
}

type Book {
  id: ID
  title: String
  pageCount: Int
  author: Author
}

type Author {
  id: ID
  firstName: String
  lastName: String
}
```

- 모델 클래스들 추가: src/main/java/com/example/graphqljavademo/bookdetails/Author.java, Book.java

```
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
```

```
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
```

- src/main/java/com/example/graphqljavademo/bookdetails/GraphQLDataFetchers.java 추가

```
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
```

- src/main/java/com/example/graphqljavademo/bookdetails/GraphQLProvider.java 추가

```
@Component
public class GraphQLProvider {

    private GraphQL graphQL;

    @Autowired
    private GraphQLDataFetchers graphQLDataFetchers;

    @Bean
    public GraphQL graphQL() {
        return graphQL;
    }

    @Bean
    public ExecutionInputCustomizer executionInputCustomizer() {
        return new ExecutionInputCustomizer() {

            @Override
            public CompletableFuture<ExecutionInput> customizeExecutionInput(
                    ExecutionInput executionInput, WebRequest webRequest) {
                executionInput.getGraphQLContext().put("username", "j.doe");
                return CompletableFuture.completedFuture(executionInput);
            }
            
        };
    }

    @PostConstruct
    public void init() throws IOException {
        URL url = Resources.getResource("schema.graphqls");
        String sdl = Resources.toString(url, Charsets.UTF_8);
        GraphQLSchema graphQLSchema = buildSchema(sdl);
        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }

    private GraphQLSchema buildSchema(String sdl) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring() {
        return newRuntimeWiring()
                .type(newTypeWiring("Query")
                        .dataFetcher("books", graphQLDataFetchers.getBooksDataFetcher())
                        .dataFetcher("bookById", graphQLDataFetchers.getBookByIdDataFetcher())
                        .dataFetcher("authors", graphQLDataFetchers.getAuthorsDataFetcher())
                        )
                .type(newTypeWiring("Mutation")
                        .dataFetcher("createAuthor", graphQLDataFetchers.getCreateAuthorFetcher()))
                .type(newTypeWiring("Book")
                        .dataFetcher("author", graphQLDataFetchers.getAuthorDataFetcher()))
                .build();
    }
}
```

