package ch.admin.foitt.openid4vc.domain.model.sdjwt.mock

import java.time.Instant

internal object JwtTimestamps {
    /*
{
   ...
   "exp":0
   "iat":0
   "nbf":0
   ...
}
     */
    const val VALID_JWT =
        "eyJhbGciOiJIUzI1NiJ9.eyJuYmYiOiIwIiwiZXhwIjoiMCIsImlhdCI6IjAifQ.TcHBuAnqn674tURVXgMzfj9xp5-YEuF2fCpMJeUVFA8"

    const val NO_EXPIRED_AT_JWT =
        "eyJhbGciOiJIUzI1NiJ9.eyJuYmYiOiIwIiwiaWF0IjoiMCJ9.T-nmTjjRlBd9_Z8h3NME8nl2qBHuojREojB_K7cnIjs"

    const val NO_ISSUED_AT_JWT =
        "eyJhbGciOiJIUzI1NiJ9.eyJuYmYiOiIwIiwiZXhwIjoiMCJ9.88FgmPh48EArI2XKnBGTqRTJ0vlv6GRy-Bmis5YGXVQ"

    const val NO_ACTIVATED_AT_JWT =
        "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOiIwIiwiaWF0IjoiMCJ9.oLl4hT_sCkaOf4yd7-r08tDorikTWQmEmUrHXW_T7PM"

    val VALID_TIMESTAMP: Instant = Instant.ofEpochSecond(0)

    /*
{
    "test": "test"
}
     */
    const val JWT_WITHOUT_TIMESTAMPS =
        "eyJhbGciOiJIUzI1NiJ9.eyJ0ZXN0IjoidGVzdCJ9.7NM6wkxPaLPEqzRtY0vHJxcAgXgUpcAm2Ihsw7-4TYc"
}
