## Reader Application
This application aggregates User, Group and Azure assigned roles from Azure AD and dumps them to a xlsx file.

The SDK and API used:
* https://docs.microsoft.com/en-us/graph/overview
* https://docs.microsoft.com/en-us/rest/api/azure/

### Prerequisite
https://docs.microsoft.com/en-us/rest/api/azure/#register-your-client-application-with-azure-ad
```
azure.app-subscription-id=<Your Azure subscription ID>
azure.app.client-id=<You registered application ID>
azure.oauth.authorization-uri=<outh2 endpoint for your registered application> # Found under "Endpoints"
```

### Build:
```
mvn clean package
```

### Run:
```
java -jar target/<artifact>.jar
```