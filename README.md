# An XTP + Kafka example project

## Requirements

- XTP CLI
- JDK 17+
- Docker (or similar) for the Kafka test container

## Structure


## Building Plugins

- Each plugin is in its own folder under `plugins`
- Customize their `xtp.toml` with:

```
app_id = "<your-app-id>"
bin = "dist/plugin.wasm"
extension_point_id = "<your-extension-point-id>"
```

`app_id` and `extension_point_id` for your own app
- Build each plug-in with `xtp plugin build` or run `make` from the top directory (`plugins`)

## Building and Starting the Java Service

Create a `.env` file in the root of the `app` directory with the following contents:

```
xtp.token=<your-xtp-token>
xtp.guest-key=<your-guest-key>
xtp.extension-point=<your-extension-point-id>
xtp.user=<your-user-id>
```

The user ID is a string that looks like `usr_<alphanumeric-string>`

```
cd app
./mvnw verify
```

### Starting
```
cd app
./mvnw quarkus:dev 
```

Kafka will listen on `localhost:9092`.

### Building a Native Image
```
cd app
./mvnw package -Dnative 
```