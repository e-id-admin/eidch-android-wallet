import com.dynatrace.tools.android.dsl.DynatraceExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.Actions.with
import org.gradle.kotlin.dsl.configure

internal class DynatraceConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            kotlin.with(pluginManager) {
                apply("com.dynatrace.instrumentation.module")
            }
            extensions.configure<DynatraceExtension> {
                // gradle task "printVariantAffiliation" prints which Dynatrace variant configuration is used for each variant

                configurations {
                    create("sandbox") {
                        variantFilter("sandbox")
                        enabled(false)
                    }

                    create("debug") {
                        variantFilter("Debug")
                        enabled(false)
                    }

                    create("release") {
                        variantFilter("Release")
                        autoStart {
                            applicationId(properties.getOrDefault("DYNATRACE_APP_ID", "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa") as String)
                            beaconUrl(properties.getOrDefault("DYNATRACE_BEACON_URL", "https://example.org/mbeacon") as String)
                        }
                        userOptIn((properties.getOrDefault("DYNATRACE_PRIVACY_OPTIN_MODE", "true") as String).toBoolean())
                        debug {
                            agentLogging((properties.getOrDefault("DYNATRACE_DEBUG_LOGS", "false") as String).toBoolean())
                        }
                        userActions {
                            enabled(false)
                            composeEnabled(false)
                        }
                        behavioralEvents {
                            detectRageTaps(false)
                        }
                        agentBehavior {
                            startupLoadBalancing(true)
                        }
                        crashReporting(true)
                        webRequests.enabled(false)
                        lifecycle.enabled(false)
                        locationMonitoring(false)
                    }
                }
            }
        }
    }
}
