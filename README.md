## Reader Application
This application aggregates User, Group and Azure assigned roles for each user and group from Azure AD and dumps them to a xlsx file.

The SDK and API used:
* https://docs.microsoft.com/en-us/graph/overview
* https://docs.microsoft.com/en-us/rest/api/azure/

### Prerequisite
https://docs.microsoft.com/en-us/rest/api/azure/#register-your-client-application-with-azure-ad
```
azure.app-subscription-id=<Your Azure subscription ID>
azure.app.client-id=<Your registered application ID>
azure.oauth.authorization-uri=<outh2 endpoint for your registered application> # Found under "Endpoints" in your app overview page on Azure
```

### Authentication & authorization
OAuth 2.0 and OpenID Connect protocols on the Microsoft identity platform
* https://docs.microsoft.com/en-us/azure/active-directory/develop/active-directory-v2-protocols
* https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-implicit-grant-flow

### Build:
```
mvn clean package
```

### Run:
```
java -jar target/<artifact>.jar
```