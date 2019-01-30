# Copy Pasta
[![Build Status](https://travis-ci.org/MeiSign/Copy-Pasta.svg?branch=master)](https://travis-ci.org/MeiSign/Copy-Pasta)

This repository is first and foremost a fun project to check 
out what Kotlin has to offer. Apart from this purpose the idea of
copy pasta is to transfer files from you phone to your desktop without
any app, wire connection or platform dependent tools.

## Run the App
Easiest way to run the app is from IDE or command line:

`mvn spring-boot:run` 

Running the app without any profile requires you to setup your AWS Accounts with credentials for S3 access and bucket.
For more information you can check out the [Spring Cloud AWS documentation](https://cloud.spring.io/spring-cloud-aws/spring-cloud-aws.html#_sdk_credentials_configuration).
If you want to run the app locally without any AWS or internet dependency you can use the **local** profile:

`mvn spring-boot:run -Dspring.profiles.active=local`

This will run the app and simulate download/upload by saving the files to the *upload-dir* in your project root.
