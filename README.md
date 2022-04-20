# ZIO EXPERIMENT

This is an experiment with ZIO + HTTP4s + DOOBIE. It is fully described in a series of posts
in [MEDIUM](https://medium.com/@supermanue). It can be regarded as the continuation
of [THIS BLOG POST](https://medium.com/@wiemzin/zio-with-http4s-and-doobie-952fba51d089)
which serves as the inspiration of this project.

The basic idea is to have a server running with that software stack. I want to make it as production-like as possible,
so I have refactored the original code adding some extra layers and testing.

## Intro

What this project does is start a small server with 2 endpoints: one allows to write a User, and the other to retrieve
it. There are example CURLS defining the functionality:

```shell
curl --location --request POST 'localhost:8083/' \
--header 'Content-Type: application/json' \
--data-raw '{
    "id": "1",
    "name": "manuel"
}'
```

```shell
curl --location --request GET 'localhost:8083/1'
```

```shell
curl --location --request DELETE 'localhost:8083/1'
```

## Architecture and code designn

My proposed architecture is:

- Main: file that connects the wires and runs the server
- HTTP: package with the HTTP server
- DOMAIN: package with the Domain logic
    - Model:
        - objects to be managed by the domain.
            - For an in-depth description of the employed data types
              see [Road To ZIO\[n\]: chapter 2, Types in the Domain layer](https://medium.com/@supermanue/road-to-zio-n-chapter-2-types-in-the-domain-layer-965c7887f1f2)
        - Errors: this is how we model the Errors in the application and deal with them in a functional way. As it is a
          small code we are keeping all in here instead of spreading them through the different layers
            - for a description of the error management
              see [Road to ZIO(n): chapter 1, the basics](https://medium.com/@supermanue/road-to-zio-n-chapter-1-e7e733e59ca4)
    - Service: business logic regarding the domain objects
    - Ports: interfaces defining the desired I/O
- ADAPTER: package with implementation for the ports
    - this is described
      in [Road to ZIO\[n\]: Chapter 4, the Persistence layer](https://medium.com/@supermanue/road-to-zio-n-chapter-4-the-persistence-layer-f268339350c8)
- CONFIGURATION: boring configuration stuff

## Storage

The project uses a H2 in-memory DB to store Users.

## Testing

Testing is performed in different layers:

- Domain: unit tests for the services residing in the Service package.
    - I am using a mock DB in here -just an array- to isolate responsibilities
    - I am using property-based testing for the model whenever possible
    - For a full description of the test
      see [Road to ZIO\[N\]: chapter 3, testing the Domain](https://medium.com/@supermanue/road-to-zio-n-chapter-3-testing-the-domain-1499ca157dc4)
- Adapters: unit tests for the adapters, making sure that they implement correctly the desired functionalities.
    - Also described in section 4
- There are missing tests described in the TODO section

## TODO

This is an ongoing process and it is still not finished. Some missing sections are:

- Clean (probably with a full refactor) the HTTP layer. This will come in section 5 of the Road to ZIO\[n\].

- Full testing of the HTTP layer with integration tests. I will start the full application and use a Scala client to
  attack the API. This way we verify that:
    - the wiring is correct and we are able to perform CRUD operations
    - the HTTP layer is correct, and the server is returning the correct HTTP codes both in the case of correct
      executions and failures

- Scalability tests
    - attack the API with something like [Gatling](https://gatling.io/) and see how ZIO+HTTP4S+DOOBIE behaves under
      heavy loads

- Kafka Producer/Consumer
    - I want to test how to write and read Kafka stuff with ZIO

- Update to ZIO2
    - while this tutorial has been writen, a new version of ZIO has been released. It is supposed to simplify most of
      the pain points, so it is definitely worth to take a deep look at it.