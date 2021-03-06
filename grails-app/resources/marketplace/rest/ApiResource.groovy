package marketplace.rest

import grails.util.GrailsUtil
import marketplace.configuration.MarketplaceApplicationConfigurationService
import ozone.marketplace.enums.MarketplaceApplicationSetting

import javax.ws.rs.GET
import javax.ws.rs.Path

@Path('/api')
class ApiResource {

    MarketplaceApplicationConfigurationService marketplaceApplicationConfigurationService
    
    @GET
    Map api() {
        String appVersion
        String buildNumber
        Date buildDate
        String name = marketplaceApplicationConfigurationService.valueOf(MarketplaceApplicationSetting.STORE_NAME)

        Properties props
        
        if('development' == GrailsUtil.environment) {
            props = readPropertiesFile('application.properties')
            appVersion = props['app.version']
            buildNumber = '-1'
            buildDate = new Date()
        }
        else {
            props = readResourcePropertiesFile('about.properties')
            appVersion = props['projectVersion']
            buildNumber = props['buildNumber']
            buildDate = new Date().parse("MMMM dd yyyy", props['buildDate'])
        }

        [
            name: name,
            version: appVersion,
            buildNumber: buildNumber,
            buildDate: buildDate
        ]
    }

    private Properties readPropertiesFile(final String filename) {
        Properties props = new Properties()
        new File(filename).withInputStream { 
            stream -> props.load(stream) 
        }
        props
    }

    private Properties readResourcePropertiesFile(final String filename) {
        Properties props = new Properties()
        Thread.currentThread().contextClassLoader.getResourceAsStream(filename).withStream {
            stream -> props.load(stream)
        }

        props
    }

}
