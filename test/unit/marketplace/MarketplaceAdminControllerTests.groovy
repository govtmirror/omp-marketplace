package marketplace

import org.hibernate.classic.Session
import org.hibernate.SessionFactory

import grails.test.mixin.web.ControllerUnitTestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin

import marketplace.testutil.FakeAuditTrailHelper

@TestMixin([ControllerUnitTestMixin, DomainClassUnitTestMixin])
abstract class MarketplaceAdminControllerTests {

    Class domainClass

    def domainInstance1, domainInstance2
    def sessionMock, sessionFactoryMock

    //override these if domain fields/constraints are different
    def params1 = [title: "title 1"]
    def params2 = [title: "title 2"]
    def tooLongTitle = "t"*51

    def validationExceptionMessage

    MarketplaceAdminControllerTests(Class controller) {
        def name = controller.name[0..-11]
        this.domainClass = Thread.currentThread().contextClassLoader.loadClass(name)
    }

    protected void setUp() {
        validationExceptionMessage = "validation failed"

        FakeAuditTrailHelper.install()

        mockDomain(domainClass)
        mockForConstraintsTests(domainClass)

        sessionMock = mockFor(Session)
        sessionFactoryMock = mockFor(SessionFactory)

        controller.sessionFactory = sessionFactoryMock.createMock()
        sessionMock.demand.setFlushMode(9) {}
        sessionFactoryMock.demand.getCurrentSession(9) { sessionMock.createMock() }

        domainInstance1 = domainClass.newInstance(params1).save(failOnError: true)
        domainInstance2 = domainClass.newInstance(params2).save(failOnError: true, flush: true)

        //mock withTransaction
        domainClass.metaClass.static.withTransaction = { it() }

        //add support for missing redirectArgs and renderArgs
        controller.metaClass.redirect = { Map map ->
            controller.metaClass.redirectArgs = map
        }
        controller.metaClass.render = { Map map ->
            controller.metaClass.renderArgs = map
        }
    }

    void testDefaultActionIsList() {
        controller.index()
        assert controller.redirectArgs.action == 'list'
    }

    void testShow() {
        controller.params.id = domainInstance1.id
        def returnMap = controller.show()

        assert returnMap."$controller.domainName".title == domainInstance1.title
    }

    void testShowFailsOnInvalidItem() {
        controller.params.id = -1
        controller.show()

        assert controller.redirectArgs.action == 'list'
        assert controller.flash.message == "specificObjectNotFound"
    }

    void testEdit() {
        controller.params.id = domainInstance1.id
        def returnMap = controller.show()

        assert returnMap."$controller.domainName".title == domainInstance1.title
    }

    void testEditFailsOnInvalidItem() {
        controller.params.id = -1
        controller.edit()

        assert controller.redirectArgs.action == 'list'
        assert controller.flash.message == "objectNotFound"
    }

    void testList() {
        def returnMap = controller.list()
        assert returnMap."${controller.domainName}List".size() == 2
        assert returnMap."${controller.domainName}Total" == 2
    }

    void testCreate() {
        controller.params.title = "title three"
        def returnMap = controller.create()
        assert returnMap."$controller.domainName".title == "title three"
    }

    void testSuccessfulSave() {

        when:
        request.method = 'POST'
        controller.params.title = "title three"
        controller.save()

        then:
        assert domainClass.count() == 3
        assert controller.flash.message == "create.success"
    }


    void testFailedSave() {

        when:
        request.method = 'POST'
        controller.params.title = " "
        controller.save()
        def nonInstance = domainClass.findByTitle(" ")

        then:
        assert nonInstance == null
        assert domainClass.count() == 2
        assert controller.flash.message == "create.failure"
    }

    void testSuccessfulUpdate() {

        when:
        request.method = 'POST'
        controller.params.id = domainInstance1.id
        controller.params.description = "new description"
        controller.update()

        then: 
        assert controller.flash.message == "update.success"
    }

    void testUpdateFailsOnObjectNotFound() {

        when:
        request.method = 'POST'
        controller.params.id = -1
        controller.update()

        then: 
        assert controller.flash.message == "objectNotFound"
    }


    void testUpdateFailsOnValidationError() {

        when:
        request.method = 'POST'
        controller.params.title = tooLongTitle
        controller.params.id = domainInstance1.id
        controller.update()

        then:
        assert controller.flash.message == "update.failure"
        assert controller.flash.args[0] == ": Error saving object"
    }

    protected void mocksForTestDelete() {}

    void testDelete() {
        mocksForTestDelete()

        when:
        request.method = 'POST'
        controller.params.id = domainInstance1.id
        controller.delete()

        then:
        assert controller.flash.message == "delete.success"
    }

    protected void mocksForTestDeleteFailure() {}

    void testDeleteFailure() {
        mocksForTestDeleteFailure()

        when:
        request.method = 'POST'
        controller.params.id = -1
        controller.delete()

        then:
        assert controller.flash.message == validationExceptionMessage
    }

    void testListAsJson() {

    }
}
