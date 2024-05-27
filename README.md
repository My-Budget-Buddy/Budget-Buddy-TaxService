# Budget Buddy: Tax Service

## Project Description
The Tax Service API for Budget Buddy allows a user to submit their earnings and withholdings for a given year and calculates their tax refund or liability. Can be used to fill in the IRS Form 1040.

## Technologies Used
* Java 17
* Spring Boot - version 3.2.5
* Spring Web
* AWS Aurora Serverless v2 for PostgreSQL
* Spring Cloud - version 2023.0.1


## Features
* Can submit and edit personal details required for the Form 1040
* Can file as Single, Married (Filing Separately), Married (Filing Jointly), Head of Household, or Qualifying Surviving Spouse
* Can submit and edit multiple W2s or Other Income forms for a given year
* Can select from among itemized and non-itemized deductions to reduce your liability
* Will automatically select the Standard Deduction or Itemized Deduction depending on which is most beneficial for the user
* Can calculate both state and federal tax liabilities and refund amounts
* User can select from among applicable Tax Credits to benefit from federal incentive programs
* Has an endpoint for retrieving a lightweight projection for the current estimated refund based on the user's financial data

To-Do List:
* Allow the user to upload photos of their tax forms to an S3 bucket and extract the data using AWS Textract
* Perform separate filings for multiple states
* Complete unit tests

## Getting Started
Use `cd` to go to the file you want to store the project in.

Clone the repository using:
`git clone git@github.com:My-Budget-Buddy/Budget-Buddy-TaxService.git`

Configure your environment variables:
* EUREKA_URL: The URL for the BudgetBuddy: Discovery Service
* DATABASE_URL: The URL for the PostgreSQL-compatible database you are using
* DATABASE_USER: The username needed to authenticate with your database
* DATABASE_PASS: The password needed to authenticate with your database

## Usage

If sending requests to the Tax Service server directly rather than through the Gateway Service you will need to include a `User-ID` in the Request Header to validate your requests.
The port is currently configured for 8084. This can be changed in the /src/main/resources/application.yml file. Assuming you are hosting locally and sending requests directly to the
Tax Service API, it functions as follows:

### Starting a new Tax Return:
1. You can view all eligible Filing Statuses with GET http://localhost:8084/taxes/taxreturns/filingStatuses
2. POST to http://localhost:8084/taxes/taxreturns
3. With the request body:
 ```
{ 
     "year": [year],
     "filingStatus": "[filing status]",
     "firstName": "[First Name]",
     "lastName": "[Last Name]",
     "email": "[email]",
     "phoneNumber": "[xxx-xxx-xxxx]",
     "address": "[address]",
     "city": "[city]",
     "state": "[XX]",
     "zip": "[zip code]",
     "dateOfBirth": "[YYYY-MM-DD]",
     "ssn": "[xxx-xx-xxxx]"
 }
```

### Editing the User Details:
1. PUT to http://localhost:8084/taxes/taxreturns/{taxreturnId}
2. With the request body:
```
{ 
   "year": [year],
   "filingStatus": "[filing status]",
   "firstName": "[First Name]",
   "lastName": "[Last Name]",
   "email": "[email]",
   "phoneNumber": "[xxx-xxx-xxxx]",
   "address": "[address]",
   "city": "[city]",
   "state": "[XX]",
   "zip": "[zip code]",
   "dateOfBirth": "[YYYY-MM-DD]",
   "ssn": "[xxx-xx-xxxx]"
 }
```
3. Note: This will replace everything that was previously stored, even fields not included in this request.

### Submitting W2s:
1. POST 


## Contributors
* Quentin Hardwick
* 

## License
This project uses the following license: <license_name>.
