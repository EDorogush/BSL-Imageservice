# BSL-Imageservice

This is a technical assignment for a backend developer from [de Bijenkorf](https://www.werkenbijdebijenkorf.nl).

## Prerequisites

* OpenJDK 11
* Postrges
* AWS account

## Run

```
./mvnw spring-boot:run

```
## Endpoints

### Get image

```
GET /image/show/{imageType}/{dummySeoName}?reference={imageName}
```

### Delete image

```
DELETE /image/flush/{imageType}?reference={imageName}
```

Where

* {imageType} should be one of ['thumbnail', 'technical-drawing', 'icon'].
* {dummySeoName} is optional, and non-used parameter
* {imageName} is unique file name and/or relative path to identify the original image on the source domain.

## Spring profiles

There are 2 spring profiles `prod` (default) and `dev`. The main difference is logging configuration:

* `prod` logs are written to the database.
* `dev` logs are written to the console.

### Logging within `prod` profile

To start application in `prod` profile next environment properties need to be configured:

*Key* |  *Description*
---|---
logdb-endpoint |  The database URL for writing logs to. Example: `jdbc:postgresql://localhost:5432/loggingdb?charSet=UNICODE`.
logdb-username |  The log database username.
logdb-password |  The log database password.

### Database for logging.

Table `application_log` where all logs are written to:

```
 CREATE TABLE application_log
(
EVENT_ID                 VARCHAR(50)
CONSTRAINT pk_application_log
PRIMARY KEY,
EVENT_DATE         timestamp,
LEVEL              varchar(50),
LOGGER             VARCHAR(10),
MESSAGE            VARCHAR(255),
THROWABLE          VARCHAR(100)
);
```

Sequence `serial` is used when new record is inserted:

```
CREATE SEQUENCE serial START 1;
```

NOTE: Next privileges must be granted to `logdb-username`:

* INSERT, SELECT, DELETE, UPDATE on table `application_log`;
* USAGE on sequence `serial`.

## Configuration properties

*Key* | *Default value* | *Description*
---|---|---
fileStorage.retry.max-attempts | 2 | The number of attempts to add image to AWS S3 bucket if first attempt failed.
fileStorage.retry.await-before-retry-ms | 200 | The delay time (ms) before next attempt.
fileStorage.amazonS3.bucket | bucketName | AWS S3 bucket name where images are stored.
source-root-url | https://i.imgflip.com | The root url where to get the images from. Default value is open free source of images. List of images allowed could be received by next request: https://api.imgflip.com/get_memes 

## Amazon S3 bucket configuration

Before running application AWS S3 bucket must be created. This could be done manually via 
[S3 Management console](https://s3.console.aws.amazon.com/) or with terraform. 
IAM User with next set of privileges must be added:
```
"s3:GetObject",
"s3:PutObject",
"s3:DeleteObject",
"s3:DeleteObject",
"s3:DeleteBucket",
"s3:CreateBucket",
"s3:ListBucket",
```

To allow application to connect to S3 storage accesskey and secretkey for user must be generated and saved in one
of next [ways](https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html):

* Environment Variables - AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY
* Java System Properties - aws.accessKeyId and aws.secretKey
* Web Identity Token credentials from the environment or container
* Credential profiles file at the default location (~/.aws/credentials) shared by all AWS SDKs and the AWS CLI

### Terraform

NOTE: to allow terraform perform action in your aws account `Administrator` privileges [must be provided](https://registry.terraform.io/providers/hashicorp/aws/latest/docs#shared-credentials-file).

Terraform variables:

*Key* |  *Description*
---|---
region |  The region where S3 storage will be created.

To init Terraform in /terraform directory run command:
```
terraform init
```
To build AWS structure use command:
```
terraform apply
```