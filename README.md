# Passos do Impeachment (RESTful service)

A *full* **demo** RESTful service to server data to the [Passos do Impeachment Android app](https://github.com/AranHase/PassosDoImpeachment-app). Also, a **webapp** using [React](https://facebook.github.io/react/) to manage the data. The server is mainly implemented using [Dropwizard](http://www.dropwizard.io/0.9.2/docs/).

## Setup and Running

1. Setup a Postgres configuration in your machine.
2. Create a database.
3. Change the `configuration.yml` to match your setup.
    1. Refer to the Dropwizard docs on how to do it: [link](http://www.dropwizard.io/0.9.2/docs/manual/configuration.html)
4. Install node.
5. Go to the `src/main/resources/webapp` folder and:

    ```
    $ npm install
    # To build once:
    $ npm run package
    # or to automatically build the webapp:
    $ npm run autopackage
    ```
    
6. Add the server URL (the same used in `configuration.yml`) to the `ServerInfo.js` file. This is the URL the web app will use to fetch the data.

7. Go back to the root directory of the project (where `configuration.yml` is located), and run the database migrations:

    ```
    # Check if everything is ready to migrate
    $ ./gradlew dbStatus
    # If okay, then proceed with the migration
    $ ./gradlew dbMigrate
    ```

8. Run the server :)

    ```
    $ ./gradlew run
    ```

### Extra

The `di-passgen` module can be used to generate SQL queries to create user credentials. Just run like this:


    $ cd di-passgen
    $ ./gradlew run -PappArgs="['myusername', 'mypassword', 'ADMIN']"
    
The above command will create a SQL query with a salted/hashed password. Copy and paste it into your postgres database. The available roles are: `USER`, `EDITOR`, `ADMIN`.

## License

Passos do Impeachment (server) is published under the Apache 2.0 license.
