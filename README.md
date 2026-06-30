#  p4pa-migration

This application represents an Utility to be deployed together with **Piattaforma Unitaria** product in order to facilitate the import of brokers data.

See [PU Microservice Architecture](https://raw.githubusercontent.com/pagopa/p4pa-doc/refs/heads/main/reference/technical-docs/Architettura_microservizi.pdf) for more details.

See [p4pa-doc](https://github.com/pagopa/p4pa-doc) for further documentation.

## 🧱 Role

* To handle the massive import of data from a previous installation.

## 🌐 APIs
See [OpenAPI](openapi/generated.openapi.json):
* Complete OpenApi;
  See [OpenAPI](openapi/generated-migration.openapi.json):
* APIs to be exposed on the internet.

Both exposed through the path:
* `/swagger-ui/index.html`

### 📌 Relevant APIs
* `POST /migration/organization/{orgIpaCode}/{migrationFileType}`: To upload a file;
* `GET /migration/organization/{orgIpaCode}`: To retrieve all the uploaded files and their status;
* `GET /migration/organization/{orgIpaCode}/migrations/{uploadId}`: To retrieve the status of a specific uploaded file;
* `GET /migration/organization/{orgIpaCode}/migrations/{uploadId}/details`: To retrieve the details of a specific uploaded file;
* `GET /migration/organization/{orgIpaCode}/migrations/{uploadId}/details/{uploadDetailsId}`: To retrieve the details of a specific uploaded file detail;
* `GET /migration/organization/{orgIpaCode}/migrations/{uploadId}/errors`: To retrieve the errors of a specific uploaded file.

### 📌 Common HTTP status returned:
* `200`: Successful operation;
* `401`: Invalid access token provided, thus a new login is required;
* `403`: Trying to access a not authorized resource.

## 🔎 Monitoring
See available actuator endpoints through the following path:
* `/actuator`

### 📌 Relevant endpoints
* Health (provide an accessToken to see details): `/actuator/health`
  * Liveness: `/actuator/health/liveness`
  * Readiness: `/actuator/health/readiness`
* Metrics: `/actuator/metrics`
  * Prometheus: `/actuator/prometheus`

Further endpoints are exposed through the JMX console.

## ✏️ Logging
See [log configured pattern](/src/main/resources/logback-spring.xml).

## 🔗 Dependencies

### 🗄️ Resources
* Shared folder
* PostgreSQL
* Temporal.io

### 🧩 Microservices
* [p4pa-auth](https://github.com/pagopa/p4pa-auth): To validate user session;
* [p4pa-organization](https://github.com/pagopa/p4pa-organization): To retrieve organization info;
* [p4pa-fileshare](https://github.com/pagopa/p4pa-fileshare): To upload file into PU and retrieve error files;
* [p4pa-process-executions](https://github.com/pagopa/p4pa-process-executions): To retrieve file processing status and details;
* [p4pa-debt-positions](https://github.com/pagopa/p4pa-debt-positions): To retrieve DeptPositionTypeOrg.

## 🗃️ Entities handled
* `uploads`: Uploaded files;
* `upload_details`: Uploaded file details;
* `debt_position_type_org_operator`: Operators loaded during migration process.

## 🔧 Configuration

See [application.yml](src/main/resources/application.yml) for each configurable property.

### 📌 Relevant configurations

#### 🌐 Application Server
| ENV         | DESCRIPTION                       | DEFAULT |
|-------------|-----------------------------------|---------|
| SERVER_PORT | Application server listening port | 8080    |

#### ✏️ Logging
| ENV                                   | DESCRIPTION                                                                                                                                            | DEFAULT |
|---------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|---------|
| LOG_LEVEL_ROOT                        | Base level                                                                                                                                             | INFO    |
| LOG_LEVEL_PAGOPA                      | Base level of custom classes                                                                                                                           | INFO    |
| LOG_LEVEL_SPRING                      | Level applied to Spring framework                                                                                                                      | INFO    |
| LOG_LEVEL_SPRING_BOOT_AVAILABILITY    | To print availability events                                                                                                                           | DEBUG   |
| LOGGING_LEVEL_API_REQUEST_EXCEPTION   | Level applied to APIs exception                                                                                                                        | INFO    |
| LOG_LEVEL_PERFORMANCE_LOG             | Level applied to [PerformanceLog](https://raw.githubusercontent.com/pagopa/p4pa-doc/refs/heads/main/reference/technical-docs/Logging.pdf)              | INFO    |
| LOG_LEVEL_PERFORMANCE_LOG_API_REQUEST | Level applied to [API Performance Log](https://raw.githubusercontent.com/pagopa/p4pa-doc/refs/heads/main/reference/technical-docs/Logging.pdf)         | INFO    |
| LOG_LEVEL_PERFORMANCE_LOG_REST_INVOKE | Level applied to [REST invoke Performance Log](https://raw.githubusercontent.com/pagopa/p4pa-doc/refs/heads/main/reference/technical-docs/Logging.pdf) | INFO    |

#### 🔁 Integrations

##### 🗄️ Resources##### 🗄️ Resources
| ENV                                                 | DESCRIPTION                                                                   | DEFAULT                                                                                                                      |
|-----------------------------------------------------|-------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------|
| SHARED_FOLDER_ROOT                                  | Absolute path towards shared folder on file system                            | /shared                                                                                                                      |
| TMP_FOLDER                                          | Absolute path towards temporary folder on file system                         | /tmp                                                                                                                         |
| SHOW_SQL                                            | To print SQL statements                                                       | false                                                                                                                        |
| MIGRATION_DB_URL                                    | PostgreSQL connection string (to use in order to customize the entire string) | jdbc:postgresql://${CLASSIFICATION_DB_HOST}:${CLASSIFICATION_DB_PORT}/${CLASSIFICATION_DB_NAME}?currentSchema=debt_positions |
| MIGRATION_DB_HOST                                   | PostgreSQL Host                                                               | localhost                                                                                                                    |
| MIGRATION_DB_PORT                                   | PostgreSQL port                                                               | 5432                                                                                                                         |
| MIGRATION_DB_NAME                                   | PostgreSQL Database name                                                      | payhub                                                                                                                       |
| MIGRATION_DB_USER                                   | PostgreSQL username                                                           |                                                                                                                              |
| MIGRATION_DB_PASSWORD                               | PostgreSQL password                                                           |                                                                                                                              |
| MIGRATION_DB_CONNECTION_IDLE_TIMEOUT_MILLISECONDS   | PostgreSQL connection idle timeout (milliseconds)                             | 600000                                                                                                                       |
| MIGRATION_DB_CONNECTION_TIMEOUT_MILLISECONDS        | PostgreSQL connection timeout (milliseconds)                                  | 30000                                                                                                                        |
| MIGRATION_DB_CONNECTION_KEEPALIVE_TIME_MILLISECONDS | PostgreSQL connection keepalive time (milliseconds)                           | 120000                                                                                                                       |
| MIGRATION_DB_CONNECTION_MAX_LIFETIME_MILLISECONDS   | PostgreSQL connection max lifetime (milliseconds)                             | 1800000                                                                                                                      |
| MIGRATION_DB_CONNECTION_MAX_POOL_SIZE               | PostgreSQL connection max pool size                                           | 10                                                                                                                           |
| MIGRATION_DB_CONNECTION_MIN_IDLE                    | PostgreSQL connection min idle                                                | 10                                                                                                                           |

##### 🔗 REST
| ENV                                               | DESCRIPTION                               | DEFAULT |
|---------------------------------------------------|-------------------------------------------|---------|
| DEFAULT_REST_CONNECTION_POOL_SIZE                 | Default connection pool size              | 10      |
| DEFAULT_REST_CONNECTION_POOL_SIZE_PER_ROUTE       | Default connection pool size per route    | 5       |
| DEFAULT_REST_CONNECTION_POOL_TIME_TO_LIVE_MINUTES | Default connection pool TTL (minutes)     | 10      |
| DEFAULT_REST_TIMEOUT_CONNECT_MILLIS               | Default connection timeout (milliseconds) | 120000  |
| DEFAULT_REST_TIMEOUT_READ_MILLIS                  | Default read timeout (milliseconds)       | 120000  |

##### 🧩 Microservices
| ENV                                      | DESCRIPTION                                         | DEFAULT |
|------------------------------------------|-----------------------------------------------------|---------|
| AUTH_BASE_URL                            | Auth microservice URL                               |         |
| AUTH_MAX_ATTEMPTS                        | Auth API max attempts                               | 3       |
| AUTH_WAIT_TIME_MILLIS                    | Auth retry waiting time (milliseconds)              | 500     |
| AUTH_PRINT_BODY_WHEN_ERROR               | To print body when an error occurs                  | true    |
| ORGANIZATION_BASE_URL                    | Organization microservice URL                       |         |
| ORGANIZATION_MAX_ATTEMPTS                | Organization API max attempts                       | 3       |
| ORGANIZATION_WAIT_TIME_MILLIS            | Organization retry waiting time (milliseconds)      | 500     |
| ORGANIZATION_PRINT_BODY_WHEN_ERROR       | To print body when an error occurs                  | true    |
| PROCESS_EXECUTIONS_BASE_URL              | ProcessExceutions microservice URL                  |         |
| PROCESS_EXECUTIONS_MAX_ATTEMPTS          | ProcessExceutions API max attempts                  | 3       |
| PROCESS_EXECUTIONS_WAIT_TIME_MILLIS      | ProcessExceutions retry waiting time (milliseconds) | 500     |
| PROCESS_EXECUTIONS_PRINT_BODY_WHEN_ERROR | To print body when an error occurs                  | true    |
| FILE_SHARE_BASE_URL                      | Fileshare microservice URL                          |         |
| FILE_SHARE_MAX_ATTEMPTS                  | Fileshare API max attempts                          | 3       |
| FILE_SHARE_WAIT_TIME_MILLIS              | Fileshare retry waiting time (milliseconds)         | 500     |
| FILE_SHARE_PRINT_BODY_WHEN_ERROR         | To print body when an error occurs                  | true    |
| DEBT_POSITION_BASE_URL                   | DebtPosition microservice URL                       |         |
| DEBT_POSITION_MAX_ATTEMPTS               | DebtPosition API max attempts                       | 3       |
| DEBT_POSITION_WAIT_TIME_MILLIS           | DebtPosition retry waiting time (milliseconds)      | 500     |
| DEBT_POSITION_PRINT_BODY_WHEN_ERROR      | To print body when an error occurs                  | true    |

##### 🕒 Temporal.io
| ENV                                                       | DESCRIPTION                                                            | DEFAULT      |
|-----------------------------------------------------------|------------------------------------------------------------------------|--------------|
| TEMPORAL_SERVER_HOST                                      | Temporal hostname                                                      | localhost    |
| TEMPORAL_SERVER_PORT                                      | Temporal port                                                          | 7233         |
| TEMPORAL_SERVER_ENABLE_HTTPS                              | To use HTTPS when invoking Temporal                                    | false        |
| TEMPORAL_SERVER_NAMESPACE                                 | Temporal namespace                                                     | pu-analytics |
| TEMPORAL_TIMEOUT_SYSTEM_INFO_SECONDS                      | Timeout set to wait for SystemInfo invokes (seconds)                   | 5            |
| TEMPORAL_TIMEOUT_RPC_LONG_POLL_SECONDS                    | Timeout set to wait for long poll RPCs (seconds)                       | 70           |
| TEMPORAL_TIMEOUT_RPC_QUERY_SECONDS                        | Timeout set to wait for query RPCs (seconds)                           | 10           |
| TEMPORAL_TIMEOUT_RPC_GENERIC_SECONDS                      | Timeout set to wait for other RPCs (seconds)                           | 10           |
| DEFAULT_ACTIVITY_CONFIG_START_TO_CLOSE_TIMEOUT_IN_SECONDS | Default startToClose activity timeout (seconds)                        | 300          |
| DEFAULT_ACTIVITY_CONFIG_RETRY_INITIAL_INTERVAL_IN_MILLIS  | Default initial interval to wait during retries (milliseconds)         | 1000         |
| DEFAULT_ACTIVITY_CONFIG_RETRY_BACKOFF_COEFFICIENT         | Default backoff coefficient used to increase the delay between retries | 1.5          |
| DEFAULT_ACTIVITY_CONFIG_RETRY_MAXIMUM_ATTEMPTS            | Default maximum number of retries                                      | 30           |

See `workflow.*` properties on [application.yml](src/main/resources/application.yml) to check configuration for each workflow.

###### 📥 TaskQueue poller sizes
See the following properties for poller sizes:
* `spring.temporal.workers[*].capacity.workflow-task-pollers-configuration.poller-behavior-autoscaling`
* `spring.temporal.workers[*].capacity.activity-task-pollers-configuration.poller-behavior-autoscaling`

#### 💼 Business logic

##### File
| ENV                       | DESCRIPTION                               | DEFAULT  |
|---------------------------|-------------------------------------------|----------|
| CSV_SEPARATOR_CHAR        | CSV column separator                      | ;        |
| CSV_QUOTE_CHAR            | CSV quote character                       | "        |
| ZIP_MAX_ENTRIES           | Maximum allowed number of zip entries     | 1000     |
| ZIP_MAX_UNCOMPRESSED_SIZE | Maximum uncompressed size of zipped files | 52428800 |
| ZIP_MAX_COMPRESSION_RATIO | Maximum compression ratio of zipped files | 100      |

#### 🔑 keys
| ENV                    | DESCRIPTION                                               | DEFAULT |
|------------------------|-----------------------------------------------------------|---------|
| FILE_ENCRYPT_PASSWORD  | Base64 encoded key (256 bit) used to encrypt/decrypt file |         |

## 🛠️ Getting Started

### 📝 Prerequisites

Ensure the following tools are installed on your machine:

1. **Java 21+**
2. **Gradle** (or use the Gradle wrapper included in the repository)
3. **Docker** (to build and run on an isolated environment, optional)

### 🔐 Write Locks

```sh
./gradlew dependencies --write-locks
```

### ⚙️ Build

```sh
./gradlew clean build
```

### 🧪 Test

#### 📌 JUnit
```sh
./gradlew test
```

### 🚀 Run local

```sh
./gradlew bootRun
```

### 🐳 Build & run through Docker
```sh
docker build -t <APP_NAME> .
docker run --env-file <ENV_FILE> <APP_NAME>
```

### ⚖️ Generate dependencies licenses
```sh
./gradlew generateLicenseReport
```
