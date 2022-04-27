# Package me.lusory.kframe.data.ktorm

ktorm integration root package, a part of the ktorm module

# Module ktorm

A ktorm interop module for KFrame, needs Java 11+

A `Database` component can be created with the `database` builder:

```kt
\@Component
fun customerDatabase(props: Properties): Database = database {
    jdbcUrl = props["your.app.database.customer.url"] as? String
        ?: throw RuntimeException("No connection URL specified for customer database")
}
```

A [HikariCP](https://github.com/brettwooldridge/HikariCP) connection pool is utilized in the builder.