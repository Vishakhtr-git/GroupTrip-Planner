# GroupTrip-Planner

This Java application connects to a local MySQL database named `tripmate_db` on port `3303` as user `root`.

Set the database password in the `TRIPMATE_DB_PASSWORD` environment variable before launching the application. The password is intentionally not stored in the source code.

The MySQL Connector/J driver is included in `lib/mysql-connector-j-9.4.0.jar`.
