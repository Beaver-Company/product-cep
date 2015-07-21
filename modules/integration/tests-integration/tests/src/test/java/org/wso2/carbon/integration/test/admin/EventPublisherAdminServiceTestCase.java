package org.wso2.carbon.integration.test.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.event.publisher.stub.types.BasicOutputAdapterPropertyDto;
import org.wso2.carbon.event.publisher.stub.types.EventMappingPropertyDto;
import org.wso2.carbon.event.publisher.stub.types.EventPublisherConfigurationDto;
import org.wso2.cep.integration.common.utils.CEPIntegrationTest;

import java.io.File;
import java.rmi.RemoteException;

public class EventPublisherAdminServiceTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(EventPublisherAdminServiceTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String loggedInSessionCookie = getSessionCookie();
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient(
                backendURL, loggedInSessionCookie);
        eventPublisherAdminServiceClient = configurationUtil.getEventPublisherAdminServiceClient(
                backendURL, loggedInSessionCookie);
    }

    @Test(groups = {"wso2.cep"}, description = "Testing get active event publisher configuration")
    public void testGetActiveEventPublisherConfiguration() {
        String samplePath = "outputflows" + File.separator + "sample0062";

        //Add StreamDefinition
        String streamDefinitionAsString = null;
        try {
            streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                    "org.wso2.event.sensor.stream_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);
            String eventPublisherConfig = getXMLArtifactConfiguration(samplePath, "httpJson.xml");
            eventPublisherAdminServiceClient.addEventPublisherConfiguration(eventPublisherConfig);

            EventPublisherConfigurationDto eventPublisherConfigurationDto =
                    eventPublisherAdminServiceClient.getActiveEventPublisherConfiguration("httpJson");
            Assert.assertEquals(eventPublisherConfigurationDto.getEventPublisherName(),"httpJson");
            Assert.assertEquals(eventPublisherConfigurationDto.getFromStreamNameWithVersion(),"org.wso2.event.sensor.stream:1.0.0");
            Assert.assertEquals(eventPublisherConfigurationDto.getMessageFormat(),"json");
            Assert.assertEquals(eventPublisherConfigurationDto.getCustomMappingEnabled(),false);

            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream", "1.0.0");
            eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("httpJson.xml");
        } catch (Exception e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.cep"}, description = "Testing get active event publisher configuration")
    public void testDeployWSO2EventPublisherConfiguration() {
        String samplePath = "outputflows" + File.separator + "sample0058";

        EventMappingPropertyDto timestamp = new EventMappingPropertyDto();
        timestamp.setName("time");
        timestamp.setType("long");
        timestamp.setValueOf("meta_timestamp");
        EventMappingPropertyDto isPowerSaverEnabled = new EventMappingPropertyDto();
        isPowerSaverEnabled.setName("isPowerSaving");
        isPowerSaverEnabled.setType("bool");
        isPowerSaverEnabled.setValueOf("meta_isPowerSaverEnabled");
        EventMappingPropertyDto id = new EventMappingPropertyDto();
        id.setName("id");
        id.setType("int");
        id.setValueOf("meta_sensorId");
        EventMappingPropertyDto name = new EventMappingPropertyDto();
        name.setName("name");
        name.setType("string");
        name.setValueOf("meta_sensorName");

        EventMappingPropertyDto longitude = new EventMappingPropertyDto();
        longitude.setName("longitude");
        longitude.setType("double");
        longitude.setValueOf("correlation_longitude");
        EventMappingPropertyDto latitude = new EventMappingPropertyDto();
        latitude.setName("latitude");
        latitude.setType("double");
        latitude.setValueOf("correlation_latitude");

        EventMappingPropertyDto humidity = new EventMappingPropertyDto();
        humidity.setName("humidityLevel");
        humidity.setType("float");
        humidity.setValueOf("humidity");
        EventMappingPropertyDto sensorValue = new EventMappingPropertyDto();
        sensorValue.setName("sensorReading");
        sensorValue.setType("double");
        sensorValue.setValueOf("sensorValue");

        BasicOutputAdapterPropertyDto username = new BasicOutputAdapterPropertyDto();
        username.setKey("username");
        username.setValue("admin");
        BasicOutputAdapterPropertyDto protocol = new BasicOutputAdapterPropertyDto();
        protocol.setKey("protocol");
        protocol.setValue("thrift");
        BasicOutputAdapterPropertyDto publishingMode = new BasicOutputAdapterPropertyDto();
        publishingMode.setKey("publishingMode");
        publishingMode.setValue("blocking");
        BasicOutputAdapterPropertyDto receiverURL = new BasicOutputAdapterPropertyDto();
        receiverURL.setKey("receiverURL");
        receiverURL.setValue("tcp://localhost:7661");
        BasicOutputAdapterPropertyDto password = new BasicOutputAdapterPropertyDto();
        password.setKey("password");
        password.setValue("admin");

        try {
            String streamDefinitionAsString = getJSONArtifactConfiguration(samplePath,
                    "org.wso2.event.sensor.stream_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionAsString);

            String streamDefinitionMapAsString = getJSONArtifactConfiguration(samplePath,
                    "org.wso2.event.sensor.stream.map_1.0.0.json");
            eventStreamManagerAdminServiceClient.addEventStreamAsString(streamDefinitionMapAsString);
            eventPublisherAdminServiceClient.addWso2EventPublisherConfiguration("eventPublisher",
                    "org.wso2.event.sensor.stream:1.0.0","wso2event",
                    new EventMappingPropertyDto[]{timestamp, isPowerSaverEnabled, id, name},
                    new EventMappingPropertyDto[]{longitude, latitude},
                    new EventMappingPropertyDto[]{humidity, sensorValue},
                    new BasicOutputAdapterPropertyDto[]{username, protocol, publishingMode,receiverURL, password},
                    true,"org.wso2.event.sensor.stream.map:1.0.0");
            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream","1.0.0");
            eventStreamManagerAdminServiceClient.removeEventStream("org.wso2.event.sensor.stream.map","1.0.0");
            eventPublisherAdminServiceClient.removeInactiveEventPublisherConfiguration("eventPublisher.xml");
        } catch (Exception e) {
            log.error("Exception thrown: " + e.getMessage(), e);
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
