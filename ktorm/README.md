# Package me.lusory.kframe.data.ktorm

ktorm integration root package, a part of the data-ktorm module

# Module ktorm

A ktorm interop module for KFrame

A `Database` component can be created with the `database` builder:

```kt
\@Component
fun customerDatabase(props: Properties): Database = database {
    connectionUrl = props["your.app.database.customer.url"] as? String
        ?: throw RuntimeException("No connection URL specified for customer database")
}
```