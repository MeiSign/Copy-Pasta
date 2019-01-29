package de.meisign.copypasta.storage

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.PutObjectResult
import com.amazonaws.services.s3.model.S3Object
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class AmazonClient(@Value("\${amazonProperties.region}") private val region: String,
                   @Value("\${amazonProperties.accessKey}") private val accessKey: String,
                   @Value("\${amazonProperties.secretKey}") private val secretKey: String,
                   @Value("\${amazonProperties.bucketName}") private val bucketName: String) {

  private val credentials = BasicAWSCredentials(this.accessKey, this.secretKey);
  private val s3client = AmazonS3ClientBuilder
      .standard()
      .withRegion(Regions.fromName(region))
      .withCredentials(AWSStaticCredentialsProvider(credentials))
      .build()


  fun uploadFile(fileName: String, inputStream: InputStream): PutObjectResult {
    val putObjectRequest = PutObjectRequest(bucketName, fileName, inputStream, ObjectMetadata())
    return s3client.putObject(putObjectRequest)
  }

  fun downloadFile(filePointer: FilePointer): S3Object {
    val s3Object = s3client.getObject(bucketName, filePointer.path())
    return s3Object
  }
}
