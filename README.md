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

## Error Handling

In the event of an exception during this process, a critical error is logged. Manual action will be
needed to amend the issue. It is not possible to rollback previously minted PIDs, as this
publication process is asynchronous, so the PID may already be in active use by the time the message
is received by this Publisher.  
