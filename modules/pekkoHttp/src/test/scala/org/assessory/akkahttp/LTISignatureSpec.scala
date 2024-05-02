package org.assessory.akkahttp

import org.assessory.akkahttp.Lti11Verifier.signature
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class LTISignatureSpec extends AnyFlatSpec with should.Matchers with ScalaFutures  {

  "LTI verifier" should "Verify the LTI example from http://lti.tools/oauth/" in {

    val parameters = Seq(
      "oauth_consumer_key" -> "dpf43f3p2l4k3l03",
      "oauth_token" -> "nnch734d00sl2jdk",
      "oauth_nonce" -> "kllo9940pd9333jh",
      "oauth_timestamp" -> "1191242096",
      "oauth_signature_method" -> "HMAC-SHA1",
      "oauth_version" -> "1.0",
      "size" -> "original",
      "file" -> "vacation.jpg"
    )

    val method = "GET"
    val authority = "photos.example.net"
    val port = "80"
    val scheme = "http"
    val path = "photos"

    val clientSecret = "kd94hf93k423kf44"
    val tokenSecret = "pfkkdhi9sl3r4s00"

    signature(
      method=method, scheme=scheme, authority=authority, port=port, path=path,
      parameters=parameters, clientSecret=clientSecret, tokenSecret=tokenSecret
    ) should be ("tR3+Ty81lMeYAr/Fid0kMTYa/WM=")
  }


  // Note, some data in this has been munged just to make sure we're not committing real data into GitHub
  it should "Verify a Moodle-like LTI 1.1 launch request" in {
    val parameters = Seq(
      "user_id" -> "90100",
      "lis_person_sourcedid" -> "00690000",
      "roles" -> "Instructor",
      "context_id" -> "23000",
      "context_label" -> "2021 TRIMESTER 2 COSC220",
      "context_title" -> "TRIMESTER 2 2021 COSC220 Software Development Studio 2",
      "resource_link_title" -> "Localhost test task",
      "resource_link_description" -> "The design task is done in groups -- if you get an error, it may be that your group is set up incorrectly.",
      "resource_link_id" -> "32224",
      "context_type" -> "CourseSection",
      "lis_course_section_sourcedid" -> "",
      "lis_result_sourcedid" -> """{"data":{"instanceid":"30004","userid":"90100","typeid":null,"launchid":609598239},"hash":"5ba4c0f9d9888b08a6842d49524e130c3dc1e3ed16059c8061b0b38eb8499da1"}""",
      "lis_outcome_service_url" -> "https://moodle.une.edu.au/mod/lti/service.php",
      "lis_person_name_given" -> "Anne",
      "lis_person_name_family" -> "Example",
      "lis_person_name_full" -> "Anne Example",
      "ext_user_username" -> "anexample",
      "lis_person_contact_email_primary" -> "anexample@example.com",
      "launch_presentation_locale" -> "en",
      "ext_lms" -> "moodle-2",
      "tool_consumer_info_product_family_code" -> "moodle",
      "tool_consumer_info_version" -> "2020061507.03",
      "oauth_callback" -> "about:blank",
      "lti_version" -> "LTI-1p0",
      "lti_message_type" -> "basic-lti-launch-request",
      "tool_consumer_instance_guid" -> "moodle.une.edu.au",
      "tool_consumer_instance_name" -> "UNE Moodle",
      "tool_consumer_instance_description" -> "UNE Moodle",
      "custom_mygroups" -> "$Moodle.Person",
      "launch_presentation_document_target" -> "frame",
      "launch_presentation_return_url" -> "https://moodle.une.edu.au/mod/lti/return.php?course=27000&launch_container=5&instanceid=32224&sesskey=NkLbSMv9rD"
    ) ++ Seq(
      "oauth_consumer_key" -> "UNE moodle",
      "oauth_nonce" -> "250bd9c25d2340e5dfa667de54658d3d",
      "oauth_timestamp" -> "1629355341",
      "oauth_signature_method" -> "HMAC-SHA1",
      "oauth_version" -> "1.0"
    )

    val clientSecret = "notverysecret"
    val method = "POST"
    val scheme = "http"
    val hostname = "localhost"
    val port = "8080"
    val path = "/lti1.1/course/61147cf6dbdf4746b7000000/task/611cfc522559644711400000"

    signature(
      method=method, scheme=scheme, authority=hostname, port=port, path=path,
      parameters=parameters, clientSecret=clientSecret, tokenSecret=""
    ) should be ("fLxb7R7bO2TtX7NyTWKmezYC5Bg=")
  }

}
