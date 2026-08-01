# Testing guide

## Backend tests

Run the backend suite with:

```powershell
cd .\backend\muniai-api
mvn test
```

## Frontend tests

Run the frontend suite with:

```powershell
cd .\frontend\muniai-web
npm test
```

## Relevant tests

- [backend/muniai-api/src/test/java/com/muniai/api/ApiIntegrationTest.java](../../backend/muniai-api/src/test/java/com/muniai/api/ApiIntegrationTest.java)
- [backend/muniai-api/src/test/java/com/muniai/conversation/api/ConversationApiIntegrationTest.java](../../backend/muniai-api/src/test/java/com/muniai/conversation/api/ConversationApiIntegrationTest.java)
- [backend/muniai-api/src/test/java/com/muniai/document/api/DocumentApiIntegrationTest.java](../../backend/muniai-api/src/test/java/com/muniai/document/api/DocumentApiIntegrationTest.java)
- [frontend/muniai-web/src/App.test.tsx](../../frontend/muniai-web/src/App.test.tsx)
- [frontend/muniai-web/src/DocumentManager.test.tsx](../../frontend/muniai-web/src/DocumentManager.test.tsx)
