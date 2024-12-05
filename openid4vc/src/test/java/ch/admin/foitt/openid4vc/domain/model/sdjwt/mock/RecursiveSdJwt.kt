package ch.admin.foitt.openid4vc.domain.model.sdjwt.mock

internal object RecursiveSdJwt {
    /*   {
               "_sd":[
                  "EieNhILyn4oeWiVzOIGvy3shkJgwApK4OzLTRnqU8rY"
               ],
            "_sd_alg":"sha-256"
         } */

    /* Disclosure1
        [
           "test_salt_1",
           "test_key_1",
           {
              "_sd":[
                 "QhuvIMQd5LyX8gOR3weVzSY0yGZGGHdVXY0E-NhhUfw" // digest of disclosure 2
              ]
           }
        ] */

    const val JSON = """{"test_key_1":{"test_key_2":"test_value_2"}}"""
    private const val DISCLOSURE_1 =
        "WwogICAidGVzdF9zYWx0XzEiLAogICAidGVzdF9rZXlfMSIsCiAgIHsKICAgICAgIl9zZCI6WwogICAgICAgICAiUWh1dklNUWQ1THlYOGdPUjN3ZVZ6U1kweUdaR0dIZFZYWTBFLU5oaFVmdyIKICAgICAgXQogICB9Cl0"

    const val JWT =
        "eyJhbGciOiJFUzUxMiIsInR5cCI6IkpXVCJ9.eyJfc2QiOlsiRWllTmhJTHluNG9lV2lWek9JR3Z5M3Noa0pnd0FwSzRPekxUUm5xVThyWSJdLCJfc2RfYWxnIjoic2hhLTI1NiIsImlhdCI6MTY5NzgxMjE5N30.AbRfRAj-7bvGOD8gHlMH0uQnTeZGAZJ6475cjp7o-r8o3n7EJ8UIg3oc71sE4qOAzbXv0NCei9JhZr6aSXDpLC9XAZNG7ETMtBRUnhe66Nl8oNB04kHZUpLjQHuRSpLUDJzVQb-O-B04nuyZ-QAMHUuDkrYlL8S__NGSmWdkbVpSyI3E"
    private const val DISCLOSURES = "~$DISCLOSURE_1~$Disclosure2~"
    const val SD_JWT = JWT + DISCLOSURES
}
