# ZIO EXPERIMENT

This is an experiment with ZIO + HTTP4s + DOOBIE. It is fully described in a series of posts
in [MEDIUM](https://medium.com/@supermanue). It can be regarded as the continuation
of [THIS BLOG POST](https://medium.com/@wiemzin/zio-with-http4s-and-doobie-952fba51d089)
which serves as the inspiration of this project.

The basic idea is to have a server running with that software stack. I want to make it as production-like as possible,
so I have refactored the code adding some extra layers.

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

```
curl --location --request DELETE 'localhost:8083/1'
```

## Architecture

My proposed architecture is:

- Main: file that connects the wires and runs the server
- HTTP: package with the HTTP server
- DOMAIN: package with the Domain logic
    - Model:
        - objects to be managed by the domain
        - Errors: this is how we model the Errors in the application and deal with them in a functional way. As it is a
          small code we are keeping all in here instead of spreading them through the different layers
    - Service: business logic regarding the domain objects
    - Ports: interfaces defining the desired I/O
- ADAPTERS: package with implementation for the ports
- CONFIGURATION: configuration stuff

## Storage

The project uses a H2 in-memory DB to store Users.

## Testing

Note: this is still a work in project, so testing is not complete. I'm defining my final objective here.

Testing is performed in different layers:

- Domain: unit tests for the services residing in the Service package.
    - We are using a mock DB in here -just an array- to isolate responsibilities
- Adapters: unit tests for the adapters, making sure that they implement correctly the desired functionalities.
- Integration tests. here we start the full application and use a Scala client to attack the API. This way we verify
  that:
    - the wiring is correct and we are able to perform CRUD operations
    - the "http" layer is correct, and the server is returning the correct HTTP codes both in the case of correct
      executions and failures


