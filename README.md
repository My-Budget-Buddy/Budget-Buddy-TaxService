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

Create a PostgreSQL database with the name: `tax-service`

The schema will be auto-generated when you run the program, but you can disable this feature in order to persist your data between sessions by editing the `src/main/resources/application.yml`
file:
```
 jpa:
   hibernate:
     ddl-auto: none
```

## Usage

If sending requests to the Tax Service server directly rather than through the Gateway Service you will need to include a `User-ID` in the Request Header to validate your requests.
The port is currently configured for 8084. This can be changed in the `/src/main/resources/application.yml` file. Assuming you are hosting locally and sending requests directly to the
Tax Service API, it functions as follows:

### Starting a new Tax Return:
1. You can view all eligible Filing Statuses with `GET http://localhost:8084/taxes/taxreturns/filingStatuses`
2. `POST` to `http://localhost:8084/taxes/taxreturns`
3. With the request body:
```
{ 
     "year": [year],
     "filingStatus": "[Filing Status]",
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
4. Note: This **must** be done prior to submitting financial information because all of the financial forms reference the Tax Return's ID, which can be obtained from the response:
```
{
     "id": [id],
     "year": [year],
     "filingStatus": "[Filing Status]",
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
1. `PUT` to `http://localhost:8084/taxes/taxreturns/{taxreturnId}`
2. With the request body:
```
{ 
   "year": [year],
   "filingStatus": "[Filing Status]",
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

### Find and view the current state of a Tax Return:
1. `GET http://localhost:8084/taxes/taxreturns/{taxreturnId}`

### Find and view all Tax Returns:
1. Submitted by the currently logged in User: `GET http://localhost:8084/taxes/taxreturns`
2. Submitted by the currently logged in User for a given year: `GET http://localhost:8084/taxes/taxreturns?year=[year]`

### Delete a Tax Return:
1. `DELETE http://localhost:8084/taxes/taxreturns/{taxreturnId}`
2. Note: This will also delete all documents currently associated with this Tax Return.

### Submitting W2s:
1. `POST http://localhost:8084/taxes/w2s?taxReturnId=[taxreturnID]`
2. Request body should be an array of entities:
```
 [
     {
         "taxReturnId": [id],
         "year": [year],
         "employer": "[Employer Name]",
         "state": "[XX]",
         "wages": [wages],
         "federalIncomeTaxWithheld": [federal income taxes],
         "stateIncomeTaxWithheld": [state income taxes],
         "socialSecurityTaxWithheld": [social security taxes],
         "medicareTaxWithheld": [medicare taxes]
     },
     {
         "taxReturnId": [id],
         "year": [year],
         "employer": "[Employer Name]",
         "state": "[XX]",
         "wages": [wages],
         "federalIncomeTaxWithheld": [federal income taxes],
         "stateIncomeTaxWithheld": [state income taxes],
         "socialSecurityTaxWithheld": [social security taxes],
         "medicareTaxWithheld": [medicare taxes]
     }
 ]
```

### Editing W2s:
1. You edit W2s the same way you create them.
2. `POST http://localhost:8084/taxes/w2s?taxReturnId=[taxreturnID]`
3. Request body should be an array of entities:
```
 [
     {
         "taxReturnId": [id],
         "year": [year],
         "employer": "[Employer Name]",
         "state": "[XX]",
         "wages": [wages],
         "federalIncomeTaxWithheld": [federal income taxes],
         "stateIncomeTaxWithheld": [state income taxes],
         "socialSecurityTaxWithheld": [social security taxes],
         "medicareTaxWithheld": [medicare taxes]
     },
     {
         "taxReturnId": [id],
         "year": [year],
         "employer": "[Employer Name]",
         "state": "[XX]",
         "wages": [wages],
         "federalIncomeTaxWithheld": [federal income taxes],
         "stateIncomeTaxWithheld": [state income taxes],
         "socialSecurityTaxWithheld": [social security taxes],
         "medicareTaxWithheld": [medicare taxes]
     }
 ]
```
4. This will replace the previously created list with the one submitted here.

### Deleting W2s:
1. You delete W2s the same way you create them.
2. `POST http://localhost:8084/taxes/w2s?taxReturnId=[taxreturnID]`
3. Omit the W2 you want removed from the list of entities in the request body:
```
 [
     {
         "taxReturnId": [id],
         "year": [year],
         "employer": "[Employer Name]",
         "state": "[XX]",
         "wages": [wages],
         "federalIncomeTaxWithheld": [federal income taxes],
         "stateIncomeTaxWithheld": [state income taxes],
         "socialSecurityTaxWithheld": [social security taxes],
         "medicareTaxWithheld": [medicare taxes]
     }
 ]
```
4. To delete ALL W2s from a Tax Return, send an empty list: `POST http://localhost:8084/taxes/w2s?taxReturnId=[taxreturnID]`
```
 [
    
 ]
```

### Finding and Viewing W2s:
1. Find a single W2 by its ID: `GET http://localhost:8084/taxes/w2s/{w2Id}`
2. Find all W2s associated with a Tax Return: `GET http://localhost:8084/taxes/w2s/w2?taxReturnId=1`
3. Find all W2s ever submitted by a User: `GET http://localhost:8084/taxes/w2s`
4. Find all W2s ever submitted by a User for a given year: `GET http://localhost:8084/taxes/w2s?year=[year]`

### Submit other forms of income:

### Claiming Tax Deductions:
1. To view all supported Tax Deduction: `GET http://localhost:8084/taxes/deductions`
2. You can pull the IDs for the deductions you want to claim from the response:
```
 [
     {
         "id": 1,
         "name": "Health Savings Account",
         "agiLimit": 3850.00,
         "itemized": false
     },
     {
         "id": 2,
         "name": "IRA Contributions",
         "agiLimit": 6500.00,
         "itemized": false
     },
     {
         "id": 3,
         "name": "Student Loan Interest",
         "agiLimit": 90000.00,
         "itemized": false
     },
     {
         "id": 4,
         "name": "Educator Expenses",
         "agiLimit": 300.00,
         "itemized": false
     },
     {
         "id": 5,
         "name": "Medical Expenses",
         "agiLimit": 0.08,
         "itemized": true
     },
     {
         "id": 6,
         "name": "State and Local Taxes",
         "agiLimit": 1.00,
         "itemized": true
     },
     {
         "id": 7,
         "name": "Mortgage Interest",
         "agiLimit": 1.00,
         "itemized": true
     },
     {
         "id": 8,
         "name": "Charitable Contributions",
         "agiLimit": 0.60,
         "itemized": true
     },
     {
         "id": 9,
         "name": "Casualty Losses",
         "agiLimit": 0.10,
         "itemized": true
     },
     {
         "id": 10,
         "name": "Miscellaneous Deductions",
         "agiLimit": 1.00,
         "itemized": true
     }
 ]
```
3. To claim a Deduction for a given Tax Return: `POST http://localhost:8084/taxes/taxreturns/{taxreturnId}/deductions`
4. With request body:
{
    "deduction": [deductionID],
    "amountSpent": [Amount of money spent]
}
5. This creates a Tax Return Deduction associated with the given TaxReturn. You can pull Tax Return Deduction ID from the response:
```
 {
     "id": [id],
     "taxReturn": [taxreturnId],
     "deduction": [deductionId],
     "deductionName": [deduction name],
     "itemized": [boolean],
     "amountSpent": [Amount of money spent],
     "agiLimit": [Limit of how much money can actually be deducted based on deduction type]
 }
```
6. Note: You can only claim each type of deduction once per tax return. If there were multiple occasions where, for example, "Charitable Contributions" were made, you sum the total
contributions when you make the claim.

### View Tax Return Deductions:
1. View a single claimed deduction based on the Tax Return Deduction ID: `GET http://localhost:8084/taxes/taxreturns/taxreturn/deductions/{id}`
2. View all claimed deductions for a given Tax Return: `GET http://localhost:8084/taxes/taxreturns/{taxreturnId}/deductions`

### Edit Tax Return Deductions:
1. You can edit a claimed deduction using the Tax Return Deduction ID: `PUT http://localhost:8084/taxes/taxreturns/{id}/deductions`
2. Be sure to include the TaxReturnId in the request body:
```
 {
     "taxReturn": [taxreturnId],
     "deduction": [deductionId],
     "amountSpent": [Amount of money spent]
 }
```

### Delete a Tax Return Deduction:
1. You can delete a Tax Return Deduction using its ID: `DELETE http://localhost:8084/taxes/taxreturns/taxreturn/deductions/{id}`

### Tax Credits:

## Contributors
* Quentin Hardwick
* 

## License
This project uses the following license: <license_name>.
