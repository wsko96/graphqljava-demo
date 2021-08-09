package com.example.graphqljavademo.bookdetails;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.spring.web.servlet.ExecutionInputCustomizer;

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
