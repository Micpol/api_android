package pl.netigen.coreapi.gdpr

interface IGDPRConsentConfig {
    val adMobPublisherIds: Array<String>
    val gdprConfig: GDPRConfig
}