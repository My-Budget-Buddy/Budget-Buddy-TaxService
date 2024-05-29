# Budget Buddy: Tax Service

## Project Description
The Tax Service API for Budget Buddy allows a user to submit their earnings and withholdings for a given year and calculates their tax refund or liability. Can be used to fill in the IRS Form 1040.

## Technologies Used
![](https://img.shields.io/badge/-Java-007396?style=flat-square&logo=java&logoColor=white)
![](https://img.shields.io/badge/-Spring_Boot-6DB33F?style=flat-square&logo=spring-boot&logoColor=white)
![Spring Security](https://img.shields.io/badge/-Spring_Security-6DB33F?style=flat-square&logo=spring-security&logoColor=white)
![](https://img.shields.io/badge/-PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![JUnit](https://img.shields.io/badge/-JUnit-25A162?style=flat-square&logo=junit5&logoColor=white)
![Docker](https://img.shields.io/badge/-Docker-2496ED?style=flat-square&logo=docker&logoColor=white)
![AWS](https://img.shields.io/badge/-AWS-232F3E?style=flat-square&logo=amazon-aws&logoColor=white)
![Maven](https://img.shields.io/badge/-Maven-C71A36?style=flat-square&logo=apache-maven&logoColor=white)
![Eureka](https://img.shields.io/badge/-Eureka-239D60?style=flat-square&logo=spring&logoColor=white)
![Microservices](https://img.shields.io/badge/-Microservices-000000?style=flat-square&logo=cloud&logoColor=white)


## Features
* Can submit and edit personal details required for the IRS Form 1040
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
1. Using your CLI tool, `cd` into the directory you want to store the project in.

2. Clone the repository using: `git clone git@github.com:My-Budget-Buddy/Budget-Buddy-TaxService.git`

3. Configure your environment variables if you do not wish to use the provided defaults:
   * EUREKA_URL: The URL for the BudgetBuddy: Discovery Service
   * DATABASE_URL: The URL for the PostgreSQL-compatible database you are using
   * DATABASE_USER: The username needed to authenticate with your database
   * DATABASE_PASS: The password needed to authenticate with your database
   * IMAGE_BUCKET: Name of the AWS S3 bucket used to store uploaded W2 files

4. Create a PostgreSQL database with the name: `tax-service`

   The schema will be auto-generated when you run the program, but you can disable this feature in order to persist your data between sessions by editing the `src/main/resources/application.yml`
   file:
```
 jpa:
   hibernate:
     ddl-auto: none
```

## Usage

If sending requests to the Tax Service server directly rather than through the Gateway Service you will need to include a `User-ID` in the Request Header to authenticate your requests.
The port is currently configured for `8084`. This can be changed in the `/src/main/resources/application.yml` file. Assuming you are hosting locally and sending requests directly to the
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
3. Note: This will replace everything that was previously stored. Fields not included in this request will be set to `null`.

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

### Upload an image of a W2 to S3:
1. Note: S3 bucket names are globally unique. The value of the PHOTO_BUCKET environment variable must match a bucket in the region defined in the S3Config.java class:
```
  @Configuration
  public class S3Config {
  
      @Bean
      public S3Client s3() {
          return S3Client.builder().region(Region.US_EAST_1).build();
      }
  }
```
2. `POST` to `http://localhost:8084/taxes/w2s/{w2Id}/image`
3. With a `byte[]` object in the request body. If using Postman you do this by navigating to Body and selectng the `binary` radio button.
4. Upload the image from your local machine. Multiple formats supported: jpg, png, pdf, etc.
5. You should receive a `201 Created` on success.
6. The image key should now be appended to the W2 entity if you need further confirmation.
7. Note: Given the current path dependency on the w2Id, the W2 must be created prior to uploading an image to it.

### Downloading the image
1. To retrieve the image after it has been stored, we use the w2Id to make a `GET` request to same place uri where it was posted: `GET http://localhost:8084/taxes/w2s/{w2Id}/image`.

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
4. To delete ALL W2s from a Tax Return, send an empty list:
`POST http://localhost:8084/taxes/w2s?taxReturnId=[taxreturnID]`
```
 [
    
 ]
```

### Finding and Viewing W2s:
1. Find a single W2 by its ID: `GET http://localhost:8084/taxes/w2s/{w2Id}`
2. Find all W2s associated with a Tax Return: `GET http://localhost:8084/taxes/w2s/w2?taxReturnId=1`
3. Find all W2s ever submitted by a User: `GET http://localhost:8084/taxes/w2s`
4. Find all W2s ever submitted by a User for a given year: `GET http://localhost:8084/taxes/w2s?year=[year]`

### Submit Other Forms of Income:
1. `POST http://localhost:8084/taxes/other-income`
2. Request body should be in the form of:
```
{
  "taxReturnId": [id; required],
  "longTermCapitalGains": [long term capital gains; optional],
  "shortTermCapitalGains": [short term capital gains; optional],
  "otherInvestmentIncome": [other investment income; optional],
  "netBusinessIncome": [net business income; optional],
  "additionalIncome": [additional income; optional]
}
```

### Edit Other Income
1. `PUT http://localhost:8084/taxes/other-income`
2. Using the same request body format as submitting other income, editing other income will simply update the tax return's other income values with the supplied values, leaving other values unchanged.

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

     ...

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
```
 {
     "deduction": [deductionID],
     "amountSpent": [Amount of money spent]
 }
```
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
1. Edit a claimed deduction using the Tax Return Deduction ID: `PUT http://localhost:8084/taxes/taxreturns/{id}/deductions`
2. We only edit the amount spent. Completely changing the type of deduction was deemed to fall outside of the scope of an edit:
```
 {
     "amountSpent": [Amount of money spent]
 }
```

### Delete a Tax Return Deduction:
1. You can delete a Tax Return Deduction using its ID: `DELETE http://localhost:8084/taxes/taxreturns/taxreturn/deductions/{id}`

### Submitting Tax Credits Information:
1. Submit user information relevant to tax credits with: `POST http://localhost:8084/taxes/tax-return-credit`
2. The request body should follow the format of:
```
{
    "taxReturnId": [id; required],
    "numDependents": [number of user's dependents; optional],
    "numDependentsAotc": [number of user's dependents that qualify for aotc credit; optional],
    "numChildren": [number of user's dependents that qualify for dependent care tax credit; optional],
    "childCareExpenses": [amount of user's child care expenses; optional],
    "educationExpenses": [amount of user's education expenses regarding aotc credit; optional],
    "llcEducationExpenses": [amount of user's education expenses regarding llc credit; optional],
    "iraContributions": [amount of user's IRA contributions; optional],
    "claimedAsDependent": [true/false; optional],
    "claimLlcCredit": [true/false; optional]
}
```

### Editing Tax Credits Information:
1. Edit a user's tax return's credit information with: `PUT http://localhost:8084/taxes/tax-return-credit`
2. Using the same request body format as submitting tax credit information, editing tax credit will simply update the tax return's tax credit values with the supplied values, leaving other values unchanged.

## Contributors
* Quentin Hardwick
* Fawaz Alharbi

## License
This project uses the following license: <license_name>.
