# DataCite Publisher

## Overview

The DataCite Publisher acts as the intermediary between
the [DiSSCo PID API](https://github.com/DiSSCo/handle-manager) and
the [DataCite API](https://support.datacite.org/docs/api). The Publisher receives messages from the
DiSSCo PID API through a RabbitMQ Queue. These messages contain the records of 200-400 PIDs minted
through the specimen ingestion process, and may either be a batch of PIDs for Digital Specimens or
Media Objects. These messages are mapped from the DiSSCo FDO Profile to the DataCite metadata
schema. Once messages are mapped, they are sent, one at a time, to the DataCite API via POST
request. This process informs DataCite of the minting of new DOIs that must be available within
their system.

Thanks to the RabbitMQ queue, this process is done asynchronously from the rest of the ingestion
process. The PID records are created in the DiSSCo PID API during ingestion, and are upgraded to
DataCite APIs when the message reaches this Publisher.

[Infrastructure Diagram](docs/publisher.png)

## Profiles

There are three profiles:

- `PUBLISH`: This publishes messages to DataCite (test or production environment, depending on
  configuration)
- `TEST`: The service formats a request, but does not publish messages to DataCite.
- `WEB`: Exposes a controller to recover from errors (see "Error Recovery")

### Error Recovery

To recover from errors, we include the `WEB` profile. This exposes a controller which accepts a list
of DOIs to re-send a message to DataCite.

The recovery service reads the FDO record for each handle and sends a request to DataCite, either an
update or a create.

**If Event Type is Unknown**: If it is unknown if DataCite has a record of the DOI, we may send
multiple requests to recover from the error. First, we send a POST to DataCite. If DataCite already
has a copy of this record, they will return a 422 UNPROCESSABLE ENTITY and an error message
indicating the DOI is already taken. In that case, we recover from this error and send an update
message to DataCite instead. Only in the WEB profile is this error recovery flow implemented; in the
regular flow, we assume we know if it is an update or a new DOI record, and structure the message to
DataCite accordingly. 

# Run locally

## Background Services

Running locally requires:

- Access to the rabbitmq cluster via localhost:5672
  - `kubectl port-forward -n rabbitmq rabbitmq-cluster-server-0 5672`

## Application properties

```properties
doi.prefix=Prefix for DOIs, e.g. 10.3535
doi.landing-page-media=landing page for media objects, e.g. https://disscover.dissco.eu/dm/
doi.landing-page-specimen=https://disscover.dissco.eu/ds/
spring.profiles.active=one of the above profiles
spring.rabbitmq.password=rabbitmq password
spring.rabbitmq.username=rabbitmq username
datacite.endpoint=endpoint to make datacite requests to, e.g. https://api.test.datacite.org/dois
datacite.password=datacite password
datacite.repository-id=datacite repository id
pid.endpoint=DOI api, i.e. https://api.dissco.eu/doi/v1/records
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://login-demo.dissco.eu/auth/realms/dissco
spring.security.oauth2.authorizationserver.endpoint.jwk-set-uri=
```

### Domain Object generation

DiSSCo uses JSON schemas to generate domain objects (e.g. Digital Specimens, Digital Media, etc)
based on the openDS specification. These files are stored in the
`/target/generated-sources/jsonschema2pojo directory`, and must be generated before running locally.
The following steps indicate how to generate these objects.

### Importing Up To-Date JSON Schemas

The JSON schemas are stored in `/resources/json-schemas`. The source of truth for JSON schemas is
the [DiSSCO Schemas Site](https://schemas.dissco.tech/schemas/fdo-type/). If the JSON schema has
changed, the changes can be downloaded using the maven runner script.

1. **Update the pom.xml**: The exec-maven-plugin in the pom indicated which version of the schema to
   download. If the version has changed, update the pom.
2. **Run the exec plugin**: Before the plugin can be run, the code must be compiled. Run the
   following in the terminal (or via the IDE interface):

```
mvn compile 
mvn exec:java
```

### Building POJOs

DiSSCo uses the [JsonSchema2Pojo](https://github.com/joelittlejohn/jsonschema2pojo) plugin to
generate domain objects based on our JSON Schemas. Once the JSON schemas have been updated, you can
run the following from the terminal (or via the IDE interface):

```
mvn clean
mvn jsonschema2pojo:generate
```