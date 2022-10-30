# Product Service
Service to return product info and pricing. Stores product pricing in service database and ingrates with product api.

## Running Locally
To run the service locally: `./gradlew bootRun`

## Testing
To run testing: `./gradlew test` <br/>
Note, if this if the first time running the tests, the embedded flapdoodle mongodb 
database needs to download so it looks like you testing is timing out but it is just 
downloading the files. This only happens on the first run

## Technologies Used
 - SpringBoot
   - Webflux
   - MongoDB Reactive Drivers
   - OKHttp for mock webserver
   - Embedded MongoDB for testing
 - MongoDB
 - Gradle
 - Kotlin
