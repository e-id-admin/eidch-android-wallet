package ch.admin.foitt.openid4vc.domain.model.sdjwt.mock

internal object FlatSdJwt {
    /*
{
   "_sd":[
      "YRLf606clwt4-hjyGze49ySFi6VCmwb9n5hwb4VUJSY",
      "QhuvIMQd5LyX8gOR3weVzSY0yGZGGHdVXY0E-NhhUfw",
      "ql6yBMb-5Ql1gG833J1o3poFIDLVt9Ck79astQeVYb0"
   ],
   "_sd_alg":"sha-256"
}
     */

    const val KEY_1 = "test_key_1"
    const val KEY_2 = "test_key_2"
    const val KEY_3 = "test_key_3"

    const val JSON = """{"$KEY_1":"test_value_1", "$KEY_2":"test_value_2", "$KEY_3":"test_value_3"}"""

    const val JWT =
        "eyJraWQiOiI0OTBmZDQ4NC1jNTE2LTQwNzktYmE4Mi1kYzkyZTA0NjAzZTEiLCJhbGciOiJFUzUxMiJ9.ew0KICAgIl9zZCI6Ww0KICAgICAgIllSTGY2MDZjbHd0NC1oanlHemU0OXlTRmk2VkNtd2I5bjVod2I0VlVKU1kiLA0KICAgICAgIlFodXZJTVFkNUx5WDhnT1Izd2VWelNZMHlHWkdHSGRWWFkwRS1OaGhVZnciLA0KICAgICAgInFsNnlCTWItNVFsMWdHODMzSjFvM3BvRklETFZ0OUNrNzlhc3RRZVZZYjAiDQogICBdLA0KICAgIl9zZF9hbGciOiJzaGEtMjU2Ig0KfQ.AL9bDNCRo-5El5QzTdZOzjd5BzFXhMAOHzMyNxuA8m5udB1fT0Qr9UdU6U_GPJe68KUn-OKhkHc1BygTTaxOB12qACkEk8gPuRvVzxNA2yjT2wwXPfZjvBUG0Ftmz6gE6anJnozJgOEWyFhLV6drKHzpVUYVXOOdjkDTB57rALuINv5z"
}
