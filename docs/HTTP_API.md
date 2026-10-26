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

Mote metrics:
- `packetsSent` – number of packets the mote sent (one per transmission, not per receiver).
- `packetsLost`, `packetLoss` – number and fraction of the mote's packets that no gateway
  received without collision. A packet is counted once the mote sends its next packet.
- `recentPacketLoss` – the same fraction over the mote's last 30 packets.
- `highestReceivedSignal` – the strongest signal (dBm) with which a gateway received the
  mote's latest packet. Omitted when no gateway received it.
- `shortestDistanceToGateway` – straight-line distance (m) from the mote to the nearest gateway.

### Radio model
- Path loss: the model of the DingNet paper (Provoost & Weyns, SEAMS 2019). The signal loses
  `10·n·log10(d)` dB over the distance `d`, accumulated step by step with the exponent `n` of the
  terrain it passes, plus Gaussian shadow fading per packet (`IotDomain/Characteristic.java`):

  | Terrain | n | Fading σ |
  | ------- | - | -------- |
  | Plain   | 2 | 1.5 dB   |
  | Forest  | 3 | 1.5 dB   |
  | City    | 1 | 2 dB     |

  There is no reference loss, so received powers are on a relative scale (about -25 to -70 dBm),
  matching the paper's figures and its signal-based thresholds (-48 / -42 dBm).
- Reception: a gateway decodes a packet if the received power exceeds the sensitivity on the same
  scale: -60 dBm at SF12, 2.5 dB higher per lower spreading factor (-47.5 dBm at SF7). At the
  default settings (14 dBm, SF12) every mote is in range; low power or a low spreading factor
  fails far from a gateway.
- Collisions: packets with the same spreading factor that overlap in time collide; a packet
  survives only if it is at least 6 dB stronger than the other (capture effect).

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
indicates no upper bound. The sampling rate cannot be adapted.

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
        { "name": "spreading_factor", "value": 9 }
      ]
    }
  ]
}
```

### Response
- `200 OK` – Plain text summary of the applied adaptations.
- `400 Bad Request` – Malformed input, invalid mote id, adaptation name, or
  out-of-range adaptation value. Adaptations listed before the rejected one are
  still applied.
- `409 Conflict` – Simulation is not currently running.

## GET `/execute_schema`
Returns the JSON Schema describing the `/execute` request body.

### Response
- `200 OK` – JSON Schema for the execute request payload.

## POST `/start_run`
Starts a new simulation run.

### Response
- `200 OK` – Plain text confirmation that the run has started.
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
