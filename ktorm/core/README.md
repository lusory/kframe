# Package me.lusory.kframe.data.ktorm

ktorm integration root package, a part of the ktorm-core module

# Package me.lusory.kframe.data.ktorm.sql

Schema generation for ktorm, a part of the ktorm-core module

# Module ktorm-core

A ktorm interop module for KFrame, needs Java 11+

A `Database` component can be created with the `database` builder:

```kt
\@Component
fun customerDatabase(): Database = database {
    jdbcUrl = System.getProperty("your.app.database.customer.url") as? String
        ?: throw RuntimeException("No connection URL specified for customer database")
}
```

A [HikariCP](https://github.com/brettwooldridge/HikariCP) connection pool is utilized in the builder.