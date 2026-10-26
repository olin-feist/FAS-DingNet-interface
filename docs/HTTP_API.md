# HTTP API Reference

This document describes the HTTP interface exposed by the DingNet simulator. All
responses are UTF-8 encoded. Unless specified otherwise, all successful
responses use the `application/json` media type.

## Overview

| Method | Path                         | Description                                                |
| ------ | ---------------------------- | ---------------------------------------------------------- |
| GET    | `/`                          | Health check endpoint that returns `OK`.                   |
| GET    | `/monitor`                   | Retrieves the latest monitored simulation state.           |
| GET    | `/monitor_schema`            | JSON Schema describing the `/monitor` response body.       |
| GET    | `/adaptation_options`        | Lists available adaptation options.                        |
| GET    | `/adaptation_options_schema` | JSON Schema describing the `/adaptation_options` response. |
| PUT    | `/execute`                   | Applies one or more adaptations to motes.                  |
| GET    | `/execute_schema`            | JSON Schema describing the `/execute` request payload.     |
| POST   | `/start_run`                 | Starts a new simulation run.                               |
| POST   | `/stop_run`                  | Stops the active simulation run.                           |
| GET    | `/mote_energy_usage`         | Retrieves historical transmission energy usage.            |

## GET `/`

### Response
- `200 OK` – Plain text body containing `OK`.

## GET `/monitor`
Retrieves the latest monitored state of the simulator.

### Response
- `200 OK` – JSON body describing the gateways, motes, and global
  simulation metrics. The structure is defined by the JSON Schema returned by
  [`/monitor_schema`](#get-monitorschema).

## GET `/monitor_schema`
Returns the JSON Schema describing the `/monitor` response payload.

### Response
- `200 OK` – JSON Schema for the monitor response.

## GET `/adaptation_options`
Lists the available adaptations that can be applied via the `/execute`
endpoint.

### Response
- `200 OK` – JSON body with the following structure:

```json
{
  "items": [
    {
      "name": "power",
      "description": "Determines the energy consumed by the mote for communication.",
      "minValue": -1,
      "maxValue": 15
    }
  ]
}
```

The array contains an entry for each adaptation option. Null `maxValue`
indicates no upper bound.

## GET `/adaptation_options_schema`
Returns the JSON Schema describing the `/adaptation_options` response payload.

### Response
- `200 OK` – JSON Schema for the adaptation options response.

## PUT `/execute`
Applies one or more adaptations to motes.

### Request
- `Content-Type: application/json`
- Body structure described by the JSON Schema served at
  [`/execute_schema`](#get-executeschema).

Example payload:

```json
{
  "items": [
    {
      "id": 0,
      "adaptations": [
        { "name": "power", "value": 5 },
        { "name": "sampling_rate", "value": 10 }
      ]
    }
  ]
}
```

### Response
- `200 OK` – Plain text summary of the applied adaptations.
- `400 Bad Request` – Malformed input, invalid mote id, adaptation name, or
  out-of-range adaptation value.
- `409 Conflict` – Simulation is not currently running.

## GET `/execute_schema`
Returns the JSON Schema describing the `/execute` request body.

### Response
- `200 OK` – JSON Schema for the execute request payload.

## POST `/start_run`
Starts a new simulation run.

### Query Parameters
- `seed` (integer, optional) – Seed for the simulation's random values (identifiers, start
  offsets, send intervals of mote 2, and per-packet shadow fading). Runs with the same seed get
  the same random values. Without it, a random seed is chosen. Example: `/start_run?seed=42`.

Adaptations arrive from outside at wall-clock times, so runs with an active adaptation strategy
are not guaranteed to be identical; the random values themselves are.

### Response
- `200 OK` – Plain text confirmation that includes the seed used, e.g.
  `Simulation started with seed 42.`
- `400 Bad Request` – The seed is not a whole number.
- `409 Conflict` – Simulation is already running.

## POST `/stop_run`
Stops the running simulation.

### Response
- `200 OK` – Plain text confirmation that the run has stopped.
- `409 Conflict` – Simulation is not running.

## GET `/mote_energy_usage`
Retrieves historical transmission energy usage for a single mote.

### Query Parameters
- `id` (integer, optional) – Index of the mote in the current environment. Use
  either `id` or `eui` but not both.
- `eui` (integer, optional) – DevEUI of the mote. Use either `id` or `eui` but
  not both.
- `run` (integer, optional) – Zero-based index of the simulation run. Defaults
  to the latest completed run.

### Response
- `200 OK` – JSON document describing the energy usage history.
- `400 Bad Request` – Missing or invalid parameters.
- `404 Not Found` – Mote with the specified identifier does not exist.
- `405 Method Not Allowed` – HTTP method is not `GET`.
- `409 Conflict` – Simulation environment has not been initialised.

#### Response Schema
The response matches the following JSON Schema:

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "additionalProperties": false,
  "required": ["EUI", "run", "transmissionEnergy", "totalEnergy"],
  "properties": {
    "EUI": {
      "type": "integer",
      "description": "DevEUI of the mote"
    },
    "run": {
      "type": "integer",
      "description": "Zero-based run index"
    },
    "transmissionEnergy": {
      "type": "array",
      "items": { "type": "number" },
      "description": "Energy consumed by each transmission during the run"
    },
    "totalEnergy": {
      "type": "number",
      "description": "Total transmission energy consumed in the run"
    }
  }
}
```

### Example

```
GET /mote_energy_usage?id=0&run=2 HTTP/1.1
Host: localhost:3000

HTTP/1.1 200 OK
Content-Type: application/json

{
  "EUI": 1234567890123456,
  "run": 2,
  "transmissionEnergy": [0.12, 0.09, 0.15],
  "totalEnergy": 0.36
}
```
