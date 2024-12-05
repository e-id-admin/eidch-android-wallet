package ch.admin.foitt.openid4vc.domain.model.sdjwt.mock

internal object ComplexSdJwt {
    /*   {
              "_sd": [
                "P_-7sGg8JhhlYo3fcRAhOoq1_Zd2rjx8wPQ1vA2AWfQ",
                "1zXmT_sGaLd7U-MlN5kQnivtnirQXLUdMCE0z5bFUIo",
                "o6JA9A7jNKxImn7LaeBJDmgnI1SYx3e7ex9zEdrhuwk",
                "E46dzJmlC2NeRIiaUdBmoPV83JIFThEFxK6d8hgQC0Q"
              ],
              "test_structured": {
                "_sd": [
                  "uw2NKXHd-jVZGrRFIV0vYL_k8L23de9xfNi0Q7FXTsA",
                  "pXkcIv9E4l3b2OTK9enBjQhb52PSFRR2055wZe1xEQ8",
                  "F88t3uqjfKnrSkkpGux3JpcM0dHUvNn1ubDkVk4GywU"
                ]
              },
              "test_key_undisclosed": "test_value_undisclosed",
              "_sd_alg": "sha-256",
              "iat": 1698857721
            } */

    // ["test_salt_recursive", "test_recursive", {"_sd":["GRI9EhZpLsc5F2XDmms2yjuuqNISzdgxzMgqxO0I4tQ", "fhG_hQESMlbTuFJFh09bwEm1dgU04X9m0trMg-0R7q0"]}]
    private const val RecursiveDisclosure =
        "WyJ0ZXN0X3NhbHRfcmVjdXJzaXZlIiwgInRlc3RfcmVjdXJzaXZlIiwgeyJfc2QiOlsiR1JJOUVoWnBMc2M1RjJYRG1tczJ5anV1cU5JU3pkZ3h6TWdxeE8wSTR0USIsICJmaEdfaFFFU01sYlR1RkpGaDA5YndFbTFkZ1UwNFg5bTB0ck1nLTBSN3EwIl19XQ"

    // ["test_salt_recursive_1", "test_key_recursive_1", "test_value_recursive_1"]
    private const val RecursiveDisclosure1 =
        "WyJ0ZXN0X3NhbHRfcmVjdXJzaXZlXzEiLCAidGVzdF9rZXlfcmVjdXJzaXZlXzEiLCAidGVzdF92YWx1ZV9yZWN1cnNpdmVfMSJd"

    // ["test_salt_recursive_2", "test_key_recursive_2", "test_value_recursive_2"]
    private const val RecursiveDisclosure2 =
        "WyJ0ZXN0X3NhbHRfcmVjdXJzaXZlXzIiLCAidGVzdF9rZXlfcmVjdXJzaXZlXzIiLCAidGVzdF92YWx1ZV9yZWN1cnNpdmVfMiJd"

    // ["test_salt_structured_1", "test_key_structured_1", "test_value_structured_1"]
    private const val StructuredDisclosure1 =
        "WyJ0ZXN0X3NhbHRfc3RydWN0dXJlZF8xIiwgInRlc3Rfa2V5X3N0cnVjdHVyZWRfMSIsICJ0ZXN0X3ZhbHVlX3N0cnVjdHVyZWRfMSJd"

    // ["test_salt_structured_2", "test_key_structured_2", "test_value_structured_2"]
    private const val StructuredDisclosure2 =
        "WyJ0ZXN0X3NhbHRfc3RydWN0dXJlZF8yIiwgInRlc3Rfa2V5X3N0cnVjdHVyZWRfMiIsICJ0ZXN0X3ZhbHVlX3N0cnVjdHVyZWRfMiJd"

    // ["test_salt_structured_recursive", "test_key_structured_recursive", {"_sd":["tEzyZKDToNlT_45TFWGzDjzWXrH6VF9QgMftrFPuazk", "BDw-OXqJN4yMBze97poM0Ubm-KJtEwicXiJ_ennknQE"]}]
    private const val StructuredRecursiveDisclosure =
        "WyJ0ZXN0X3NhbHRfc3RydWN0dXJlZF9yZWN1cnNpdmUiLCAidGVzdF9rZXlfc3RydWN0dXJlZF9yZWN1cnNpdmUiLCB7Il9zZCI6WyJ0RXp5WktEVG9ObFRfNDVURldHekRqeldYckg2VkY5UWdNZnRyRlB1YXprIiwgIkJEdy1PWHFKTjR5TUJ6ZTk3cG9NMFVibS1LSnRFd2ljWGlKX2VubmtuUUUiXX1d"

    // ["test_salt_structured_recursive_1", "test_key_structured_recursive_1", "test_value_structured_recursive_1"]
    private const val StructuredRecursiveDisclosure1 =
        "WyJ0ZXN0X3NhbHRfc3RydWN0dXJlZF9yZWN1cnNpdmVfMSIsICJ0ZXN0X2tleV9zdHJ1Y3R1cmVkX3JlY3Vyc2l2ZV8xIiwgInRlc3RfdmFsdWVfc3RydWN0dXJlZF9yZWN1cnNpdmVfMSJd"

    // ["test_salt_structured_recursive_2", "test_key_structured_recursive_2", "test_value_structured_recursive_2"]
    private const val StructuredRecursiveDisclosure2 =
        "WyJ0ZXN0X3NhbHRfc3RydWN0dXJlZF9yZWN1cnNpdmVfMiIsICJ0ZXN0X2tleV9zdHJ1Y3R1cmVkX3JlY3Vyc2l2ZV8yIiwgInRlc3RfdmFsdWVfc3RydWN0dXJlZF9yZWN1cnNpdmVfMiJd"

    // ["test_salt_flat", "test_key_flat", "test_value_flat"]
    private const val FlatDisclosure = "WyJ0ZXN0X3NhbHRfZmxhdCIsICJ0ZXN0X2tleV9mbGF0IiwgInRlc3RfdmFsdWVfZmxhdCJd"

    // ["test_salt_flat_array", "test_key_flat_array", ["test_value_flat_array_1", "test_value_flat_array_2"]]
    private const val FlatArrayDisclosure =
        "WyJ0ZXN0X3NhbHRfZmxhdF9hcnJheSIsICJ0ZXN0X2tleV9mbGF0X2FycmF5IiwgWyJ0ZXN0X3ZhbHVlX2ZsYXRfYXJyYXlfMSIsICJ0ZXN0X3ZhbHVlX2ZsYXRfYXJyYXlfMiJdXQ"

    // ["test_salt_flat_object", "test_key_flat_object", {"test_key_flat_object_1":"test_value_flat_object_1","test_key_flat_object_2":"test_value_flat_object_2"}]
    private const val FlatObjectDisclosure =
        "WyJ0ZXN0X3NhbHRfZmxhdF9vYmplY3QiLCAidGVzdF9rZXlfZmxhdF9vYmplY3QiLCB7InRlc3Rfa2V5X2ZsYXRfb2JqZWN0XzEiOiJ0ZXN0X3ZhbHVlX2ZsYXRfb2JqZWN0XzEiLCJ0ZXN0X2tleV9mbGF0X29iamVjdF8yIjoidGVzdF92YWx1ZV9mbGF0X29iamVjdF8yIn1d"

    const val JSON = """
{
   "test_recursive":{
      "test_key_recursive_1":"test_value_recursive_1",
      "test_key_recursive_2":"test_value_recursive_2"
   },
   "test_structured":{
      "test_key_structured_1":"test_value_structured_1",
      "test_key_structured_2":"test_value_structured_2",
      "test_key_structured_recursive":{
         "test_key_structured_recursive_1":"test_value_structured_recursive_1",
         "test_key_structured_recursive_2":"test_value_structured_recursive_2"
      }
   },
   "test_key_flat":"test_value_flat",
   "test_key_flat_array":[
      "test_value_flat_array_1",
      "test_value_flat_array_2"
   ],
   "test_key_flat_object":{
      "test_key_flat_object_1":"test_value_flat_object_1",
      "test_key_flat_object_2":"test_value_flat_object_2"
   },
   "test_key_undisclosed":"test_value_undisclosed"
}
    """

    const val JWT =
        "eyJhbGciOiJFUzUxMiIsInR5cCI6IkpXVCJ9.eyJfc2QiOlsiUF8tN3NHZzhKaGhsWW8zZmNSQWhPb3ExX1pkMnJqeDh3UFExdkEyQVdmUSIsIjF6WG1UX3NHYUxkN1UtTWxONWtRbml2dG5pclFYTFVkTUNFMHo1YkZVSW8iLCJvNkpBOUE3ak5LeEltbjdMYWVCSkRtZ25JMVNZeDNlN2V4OXpFZHJodXdrIiwiRTQ2ZHpKbWxDMk5lUklpYVVkQm1vUFY4M0pJRlRoRUZ4SzZkOGhnUUMwUSJdLCJ0ZXN0X3N0cnVjdHVyZWQiOnsiX3NkIjpbInV3Mk5LWEhkLWpWWkdyUkZJVjB2WUxfazhMMjNkZTl4Zk5pMFE3RlhUc0EiLCJwWGtjSXY5RTRsM2IyT1RLOWVuQmpRaGI1MlBTRlJSMjA1NXdaZTF4RVE4IiwiRjg4dDN1cWpmS25yU2trcEd1eDNKcGNNMGRIVXZObjF1YkRrVms0R3l3VSJdfSwidGVzdF9rZXlfdW5kaXNjbG9zZWQiOiJ0ZXN0X3ZhbHVlX3VuZGlzY2xvc2VkIiwiX3NkX2FsZyI6InNoYS0yNTYiLCJpYXQiOjE2OTg4NTc3MjF9.AYcZWBhRbS4bFymmF6gaqUZMeuT9x3GfdxrnVZAGw2sxccjusqM7hcJrdyl6dnS43hrGgPcsBcvUR48L7Cl65KpBADAU9fLQoBa_00PM8u9b3ufccy1xTXgZdxEBWgbZb784xrDXlrIdlTS_1pmsU7CuZT-5Q3PXJDBbXyXJm8TUHwHU"
    const val SD_JWT = JWT +
        "~$RecursiveDisclosure~$RecursiveDisclosure1~$RecursiveDisclosure2" +
        "~$StructuredDisclosure1~$StructuredDisclosure2" +
        "~$StructuredRecursiveDisclosure~$StructuredRecursiveDisclosure1~$StructuredRecursiveDisclosure2" +
        "~$FlatDisclosure" +
        "~$FlatArrayDisclosure" +
        "~$FlatObjectDisclosure~"
}
