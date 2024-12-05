package ch.admin.foitt.openid4vc.domain.model.sdjwt.mock

internal object StructuredSdJwt {
   /*   {
           "test":{
              "_sd":[
                 "YRLf606clwt4-hjyGze49ySFi6VCmwb9n5hwb4VUJSY",
                 "QhuvIMQd5LyX8gOR3weVzSY0yGZGGHdVXY0E-NhhUfw"
              ]
           },
           "_sd_alg":"sha-256"
        } */

    const val JSON = """{"test":{"test_key_1":"test_value_1", "test_key_2":"test_value_2"}}"""

    const val JWT =
        "eyJhbGciOiJFUzUxMiIsInR5cCI6IkpXVCJ9.eyJ0ZXN0Ijp7Il9zZCI6WyJZUkxmNjA2Y2x3dDQtaGp5R3plNDl5U0ZpNlZDbXdiOW41aHdiNFZVSlNZIiwiUWh1dklNUWQ1THlYOGdPUjN3ZVZ6U1kweUdaR0dIZFZYWTBFLU5oaFVmdyJdfSwiX3NkX2FsZyI6InNoYS0yNTYiLCJpYXQiOjE2OTc4MDY3NzF9.AHQWPMY-EwTHFzdGMpCIREvQQoXOUfD34LPoCUwshyzj-6H6209wDDeQRv8UAr8VgcEXBK0NLO9SNm-UWhrjY70FAJ3w842_D8QCEnvWB3kyDVmLd5YqUWYQgmqKRJr7qiRAWOEGvYIXw4Mo67RPwTfmzhaA-WAUW6dyPmC0Du7eb6BY"
}
