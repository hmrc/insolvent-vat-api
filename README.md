insolvent-vat-api
========================
**Add overview documentation here and use the title of the API and not the URL e.g. "Business details API" and not "business-details-api"**

## Requirements 
- Scala 2.12.x
- Java 8
- sbt 1.3.7
- [Service Manager](https://github.com/hmrc/service-manager)
 
## Development Setup
  
Run from the console using: `sbt run` (starts on port 7676 by default)
  
Start the service manager profile: `sm --start MTDFB_INSOLVENT_VAT`

# Running tests
```
sbt test
sbt it:test
```

## Viewing RAML

To view documentation locally ensure the **add api name** API is running, and run api-documentation-frontend:
`./run_local_with_dependencies.sh`

Then go to http://localhost:9680/api-documentation/docs/api/preview and use this port and version:
`http://localhost:XXXX/api/conf/1.0/application.raml`

## Reporting Issues

You can create a GitHub issue [here](**https://github.com/hmrc/insolvent-vat-api/issues**)


## API Reference / Documentation 
Available on the [Documentation](https://developer.service.hmrc.gov.uk/api-documentation) (find and link the page for specific api)


# License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
