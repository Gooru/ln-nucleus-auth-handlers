Nucleus auth handlers
=====================

This is the auth handler for Project Nucleus.

This project contains just one main verticle which is responsible for listening for Authentication, Authorize, User, UserPrefs, AuthenticationGLAVersion address on message bus.

DONE
----
* Configured listener
* Provided a initializer and finalizer mechanism for components to initialize and clean up themselves
* Created a data source registry and register it as component for initialization and finalization
* Provided Hikari connection pool from data source registry
* Processor layer is created which is going to take over the message processing from main verticle once message is read
* Logging and app configuration
* Transformer layer working for positive cases
* DB layer to actually do the operations
* Transformer layer implementation so that output from DB layer could be transformed and written back to message bus
* Incorporate ActiveJdbc into project and build
* Provided Redis connection support

**API features**
- <a  href="https://github.com/Gooru/nucleus-auth-gateway/blob/master/api-docs/AUTHENTICATION.md">AUTHENTICATION</a>
- <a href="https://github.com/Gooru/nucleus-auth-gateway/blob/master/api-docs/USER.MD">USER</a>
- <a href="https://github.com/Gooru/nucleus-auth-gateway/blob/master/api-docs/USER_PREFERENCE.MD">USER_PREFERENCE</a>
- <a href="https://github.com/Gooru/nucleus-auth-gateway/blob/master/api-docs/AUTHORIZE.MD">AUTHORIZE</a>


**service**
* Read access token from redis

TODO
----
 - Notification

To understand build related stuff, take a look at <a href="BUILD_README.md">BUILD_README</a>

