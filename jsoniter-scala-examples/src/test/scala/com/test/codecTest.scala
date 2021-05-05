package com.test

import com.github.plokhotnyuk.jsoniter_scala.core._
import com.test.Codecs._
import org.scalatest.{Matchers, WordSpec}

class codecTest extends WordSpec with Matchers {

  "decoding text " should {

    "decode bulk" in {
      val bulk =
        """{"subsystemName":"something","applicationName":"something","privateKey":"something","computerName":"something","logEntries":[{"category":"something","className":"something","methodName":"something","severity":3,"threadId":140136948304616,"timestamp":15435706484983514,"logSeed":5683,"text":{"processing_breakdown":{"fetching_messages":{"num":1,"sum":2.0020086765,"avg":2.0020086765},"no_messages_sleep":{"num":0,"sum":0,"avg":0},"processing_messages":{"subprocesses":{"index_erroneous_logs":{"subprocesses":{"error_calc_bulk_size":{"num":0,"sum":0,"avg":0}},"num":0,"sum":0,"avg":0},"update_bulk":{"subprocesses":{"update_bulk_prepare_docs":{"num":0,"sum":0,"avg":0},"update_bulk_insert_updates":{"num":0,"sum":0,"avg":0},"update_bulk_msearch_query":{"num":0,"sum":0,"avg":0}},"num":1,"sum":0.0000030994,"avg":0.0000030994},"prepare_es_doc":{"num":463,"sum":0.0176346302,"avg":0.0000380878},"prepare_es_insert_action":{"num":463,"sum":0.0154974461,"avg":0.0000334718},"index_bulk":{"subprocesses":{"calc_bulk_size":{"num":1,"sum":0.008865118,"avg":0.008865118},"index_handle_error":{"subprocesses":{"index_handle_limit_fields":{"num":0,"sum":0,"avg":0},"index_handle_mapping_exception":{"num":0,"sum":0,"avg":0}},"num":0,"sum":0,"avg":0},"send_to_es":{"num":1,"sum":0.1224887371,"avg":0.1224887371}},"num":1,"sum":0.1313719749,"avg":0.1313719749},"message_json_loads":{"num":463,"sum":0.0067739487,"avg":0.0000146306}},"num":1,"sum":0.1766602993,"avg":0.1766602993}},"class":"something","batch_size":"108.172.107.120","method":{"ip":"108.172.107.120"}},"logId":"something"}],"es_cluster":"something","sdk":{"version":"1.1.0","bulkSeed":[62336,2809963446],"companyId":33,"applicationName":"something","subsystemName":"something","computerName":"something","IPAddress":"","id":"something","es_cluster":null,"processName":null},"SDKId":"something"}"""
      val res = readFromArray[Bulk](bulk.getBytes("UTF-8"))
      new String(writeToArray(res), "UTF-8") shouldBe bulk
    }
  }
}
