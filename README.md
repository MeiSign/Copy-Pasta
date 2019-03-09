# Copy Pasta
[![Build Status](https://travis-ci.org/MeiSign/Copy-Pasta.svg?branch=master)](https://travis-ci.org/MeiSign/Copy-Pasta)

This repository is first and foremost a fun project to find
out what Kotlin has to offer. Apart from this purpose, the idea of
Copy Pasta is to transfer files from you phone to your desktop without
any app, wire connection or platform dependent tools.

## Run the App
The easiest way to run the app is from your IDE or command line:

`mvn spring-boot:run` 

Running the app without any profile requires you to setup your AWS Accounts 
with permissions for S3 and bucket operations (Get, Put, List).
For more information you can check out the [Spring Cloud AWS documentation](https://cloud.spring.io/spring-cloud-aws/spring-cloud-aws.html#_sdk_credentials_configuration).
If you want to run the app locally without any AWS or internet dependency you can use the **local** profile:

`mvn spring-boot:run -Dspring.profiles.active=local`

This will run the app with localstack to simuelate aws. 
If you can't start the app with local profile, check the localstack [documentation and requirements](https://github.com/localstack/localstack#requirements). 

### Frontend Development
If you want to run and change the frontend without restarting the backend 
with every change, you can use npm to start the frontend independently with a proxy.

`npm start` 
