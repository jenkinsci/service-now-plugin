package org.jenkinsci.plugins.servicenow

import org.jenkinsci.plugins.servicenow.model.ServiceNowConfiguration
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ServiceNowConfigurationTest {
    private lateinit var sut : ServiceNowConfiguration

    @Before
    fun `initialize configuration object`() {
        sut = ServiceNowConfiguration("test")
        sut.producerId = "abc123"
    }

    @Test
    fun `base instance should be built in standard way`() =
            assertEquals("https://test.service-now.com", sut.baseUrl)

    @Test
    fun `producer url should be built from base`() =
            assertEquals("https://test.service-now.com/api/sn_sc/servicecatalog/items/abc123/submit_producer", sut.producerRequestUrl)


}