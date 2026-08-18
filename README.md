# Columbus Events

- Sends a daily SMS message with the days events happening in the ciy.
  - Sends both events I'm interested in, and events that potentially cause traffic (to know where to avoid driving)
- AWS Lambda runs daily at 9am EST which executes this application. 
- Events with a date of today are fetched several ways:
- - DynamoDB table, populated by google form manual entries (Ex: Comfest, Cap City Marathon)
- - Web scraping venue websites (Ex: Kemba Live, Ace of Cups)
- - Discovery API (Ex: Crew Stadium, OSU Stadium, Convention Center)

## Purpose
- This Application is a pet project to help inform me of what is going on in my city, as well as an exercise to hone my Developer skills.
## Tech
- AWS infrastructure
  - Amazon Step Function (runs after columbus-events-import-lambda)
  - Lambda
  - DynamoDB
  - CodePipeline
- Twilio (sends SMS)
- JSoup (to scrape the websites)
- Google Sheet (Stores newly added events from user before being added to the database via columbus-events-import-lambda)
- Google Form (user-friendly way to add new events, which get added to the google sheet)

## Helpful info for running app
Lambda Handler calls the method that kicks off the whole process:
com.ColumbusEventAlertService.services.TextMessageService::sendTodaysEvents
### Run locally
Application is set up to run locally, using logic in the main method. Add a program argument (in the application configuration) called "localRun".
