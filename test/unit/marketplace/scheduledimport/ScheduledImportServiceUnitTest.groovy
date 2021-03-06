package marketplace.scheduledimport

import grails.test.mixin.services.ServiceUnitTestMixin
import grails.test.runtime.FreshRuntime
import grails.test.mixin.TestMixin
import grails.test.mixin.TestFor

import marketplace.ImportStatus
import marketplace.CustomFieldDefinition
import marketplace.CustomField
import marketplace.ServiceItem
import marketplace.OwfProperties
import marketplace.Profile
import marketplace.Types
import marketplace.Images
import marketplace.Agency
import marketplace.Category
import marketplace.State
import marketplace.Relationship
import marketplace.DropDownCustomFieldDefinition
import marketplace.DropDownCustomField
import marketplace.TextCustomFieldDefinition
import marketplace.TextCustomField
import marketplace.TextAreaCustomFieldDefinition
import marketplace.ImageURLCustomFieldDefinition
import marketplace.CheckBoxCustomFieldDefinition
import marketplace.FieldValue
import marketplace.ImportTaskResult
import marketplace.ImportTask
import marketplace.InterfaceConfiguration
import marketplace.Constants
import ozone.marketplace.enums.RelationshipType

import marketplace.AccountService
import marketplace.AuditLoggingService
import marketplace.rest.ProfileRestService
import marketplace.rest.CategoryRestService
import marketplace.rest.TypeRestService
import marketplace.rest.StateRestService
import marketplace.rest.ServiceItemRestService
import marketplace.rest.AgencyRestService
import marketplace.rest.DropDownCustomFieldDefinitionRestService
import marketplace.rest.TextCustomFieldDefinitionRestService
import marketplace.rest.TextAreaCustomFieldDefinitionRestService
import marketplace.rest.ImageURLCustomFieldDefinitionRestService
import marketplace.rest.CheckBoxCustomFieldDefinitionRestService

import marketplace.testutil.FakeAuditTrailHelper

import ozone.marketplace.domain.ValidationException

@Mock([
    CustomFieldDefinition,
    CustomField,
    ServiceItem,
    OwfProperties,
    Profile,
    Types,
    Images,
    Agency,
    Category,
    State,
    Relationship,
    DropDownCustomFieldDefinition,
    TextCustomFieldDefinition,
    TextAreaCustomFieldDefinition,
    ImageURLCustomFieldDefinition,
    CheckBoxCustomFieldDefinition,
    FieldValue,
    ImportTaskResult,
    ImportTask,
    InterfaceConfiguration
])
@TestFor(ScheduledImportService)
@TestMixin(ServiceUnitTestMixin)
class ScheduledImportServiceUnitTest {

    ImportTask task

    ScheduledImportData importData = new ScheduledImportData(
        profiles: [
            new Profile(username: 'testUser1'),
            new Profile(username: 'testAdmin1'),
            new Profile(username: 'System')
        ],
        types: [
            new Types(
                title: 'type 1',
                uuid: '4f2681ef-20c0-4923-b95c-f85cf52b171c',
                createdBy: new Profile(username: 'testAdmin1'),
                editedBy: new Profile(username: 'testAdmin1')
            ),
            new Types(
                title: 'type 2',
                ozoneAware: true,
                uuid: 'e46c3676-61ba-4b9c-b6d9-c08dba622e47',
                createdBy: new Profile(username: 'testAdmin1'),
                editedBy: new Profile(username: 'testAdmin1'),
                image: new Images(contentType: 'image/png')
            )
        ],
        categories: [
            new Category(
                createdBy: new Profile(username: 'testAdmin1'),
                editedBy: new Profile(username: 'testAdmin1'),
                title: 'Category 1',
                uuid: 'd7b501e2-9585-4ae6-8036-a19880b44921'
            ),
            new Category(
                createdBy: new Profile(username: 'testAdmin1'),
                editedBy: new Profile(username: 'testAdmin1'),
                title: 'Category 2',
                uuid: '76f885d0-9ae7-49a6-ad95-2d929f7c5cf7'
            )
        ],
        states: [
            new State(
                createdBy: new Profile(username: 'testAdmin1'),
                editedBy: new Profile(username: 'testAdmin1'),
                title: 'state 1',
                isPublished: true,
                uuid: '4a5e7c6b-be13-46a4-9f7e-9a28935dd929'
            ),
            new State(
                createdBy: new Profile(username: 'testAdmin1'),
                editedBy: new Profile(username: 'testAdmin1'),
                title: 'state 2',
                isPublished: false,
                uuid: '5cee51ad-4324-4aa9-9928-e2c7303c1bdc'
            )
        ],
        customFieldDefs: [
            new TextCustomFieldDefinition(
                createdBy: new Profile(username: 'testAdmin1'),
                editedBy: new Profile(username: 'testAdmin1'),
                uuid: '1958f80c-005c-4525-8147-d784af179dfd',
                name: 'Custom field 1',
                label: 'Custom field 1 label',
                types: [
                    new Types(uuid: '4f2681ef-20c0-4923-b95c-f85cf52b171c')
                ]
            ),
            new TextAreaCustomFieldDefinition(
                createdBy: new Profile(username: 'testAdmin1'),
                editedBy: new Profile(username: 'testAdmin1'),
                uuid: '2fe01abb-aef0-44f7-8a7b-e2d7454409f1',
                name: 'Custom field 2',
                label: 'Custom field 2 label',
                types: [
                    new Types(uuid: '4f2681ef-20c0-4923-b95c-f85cf52b171c')
                ]
            ),
            new CheckBoxCustomFieldDefinition(
                createdBy: new Profile(username: 'testAdmin1'),
                editedBy: new Profile(username: 'testAdmin1'),
                uuid: 'ae058c57-c636-45e9-9b9e-4e1075fe2233',
                name: 'Custom field 3',
                label: 'Custom field 3 label',
                types: [],
                allTypes: true
            ),
            new ImageURLCustomFieldDefinition(
                createdBy: new Profile(username: 'testAdmin1'),
                editedBy: new Profile(username: 'testAdmin1'),
                uuid: 'e32872e0-82ad-47bb-b246-19a80ad548f0',
                name: 'Custom field 4',
                label: 'Custom field 4 label',
                types: [
                    new Types(uuid: '4f2681ef-20c0-4923-b95c-f85cf52b171c')
                ]
            ),
            new DropDownCustomFieldDefinition(
                createdBy: new Profile(username: 'testAdmin1'),
                editedBy: new Profile(username: 'testAdmin1'),
                uuid: 'c574e73a-6006-4899-959f-4f9b8a68e6e9',
                name: 'Custom field 5',
                label: 'Custom field 5 label',
                types: [],
                allTypes: true,
                fieldValues: [
                    new FieldValue(displayText: 'value 1'),
                    new FieldValue(displayText: 'value 2', isEnabled: false)
                ]
            )
        ],
        serviceItems: [
            new ServiceItem(
                createdBy: new Profile(username: 'testAdmin1'),
                editedBy: new Profile(username: 'testAdmin1'),
                title: 'Listing 1',
                uuid: '497df2c6-ffd7-49be-a7ab-000d9ec1ddf8',
                types: new Types(uuid: '4f2681ef-20c0-4923-b95c-f85cf52b171c'),
                state: new State(uuid: '4a5e7c6b-be13-46a4-9f7e-9a28935dd929'),
                categories: [
                    new Category(uuid: 'd7b501e2-9585-4ae6-8036-a19880b44921'),
                    new Category(uuid: '76f885d0-9ae7-49a6-ad95-2d929f7c5cf7')
                ],
                owners: [
                    new Profile(username: 'testUser1')
                ],
                customFields: [
                    new CustomField(
                        customFieldDefinition: new CustomFieldDefinition(
                            uuid: '1958f80c-005c-4525-8147-d784af179dfd'
                        ),
                        value: 'test value'
                    ),
                    new CustomField(
                        customFieldDefinition: new CustomFieldDefinition(
                            uuid: 'c574e73a-6006-4899-959f-4f9b8a68e6e9'
                        ),
                        value: 'value 1'
                    )
                ],
                agency: new Agency(title: 'Agency 1', iconUrl: 'https://localhost:8443/agency')
            ),
            new ServiceItem(
                createdBy: new Profile(username: 'testAdmin1'),
                editedBy: new Profile(username: 'testAdmin1'),
                title: 'Listing 2',
                uuid: 'e520a2bc-9247-400d-8df8-ddd226853bd3',
                types: new Types(uuid: 'e46c3676-61ba-4b9c-b6d9-c08dba622e47'),
                state: new State(uuid: '5cee51ad-4324-4aa9-9928-e2c7303c1bdc'),
                categories: [
                    new Category(uuid: '76f885d0-9ae7-49a6-ad95-2d929f7c5cf7')
                ],
                owners: [
                    new Profile(username: 'testAdmin1')
                ],
                owfProperties: new OwfProperties(
                    singleton: true
                )
            )
        ],
        relationships: [
            new Relationship(
                relationshipType: null, //this won't be set by an import
                owningEntity: new ServiceItem(uuid: 'e520a2bc-9247-400d-8df8-ddd226853bd3'),
                relatedItems: [
                    new ServiceItem(uuid: '497df2c6-ffd7-49be-a7ab-000d9ec1ddf8')
                ]
            )
        ]
    )

    //simulate an update by setting the editedBy dates on various items to be the current time.
    //This function is intended to be run after performing the create
    private void makeUpdate() {
        [
            'profiles',
            'types',
            'categories',
            'states',
            'customFieldDefs',
            'serviceItems'
        ].each { prop ->
            //only update odd-indexed items
            def data = importData[prop]
            
            if (prop == 'profiles') {
                data = data.grep { it.username != Profile.SYSTEM_USER_NAME }
            }

            data.eachWithIndex { item, i -> if (i % 2 ) item.editedDate = new Date() }
        }
    }

    private void resetEditedDates() {
        [
            'profiles',
            'types',
            'categories',
            'states',
            'customFieldDefs',
            'serviceItems'
        ].each { prop ->
            importData[prop].each { it.editedDate = null }
        }
    }

    private mockImportMethods(Collection<String> except) {
        def doNothing = { Collection c, ImportStatus s -> }

        if (!except.contains('profiles')) service.metaClass.importProfiles = doNothing
        if (!except.contains('categories')) service.metaClass.importCategories = doNothing
        if (!except.contains('types')) service.metaClass.importTypes = doNothing
        if (!except.contains('states')) service.metaClass.importStates = doNothing
        if (!except.contains('serviceItems'))
            service.metaClass.importServiceItems = { Collection c, Collection c2, ImportStatus s->
            }
        if (!except.contains('relationships')) service.metaClass.importRelationships = doNothing
        if (!except.contains('customFieldDefinitions'))
            service.metaClass.importCustomFieldDefinitions = doNothing
    }

    private ScheduledImportHttpService mockHttpService() {
        [
            retrieveRemoteImportData: { importData }
        ] as ScheduledImportHttpService
    }

    private void mockRestServices() {
        [
            ProfileRestService,
            CategoryRestService,
            TypeRestService,
            StateRestService,
            ServiceItemRestService,
            AgencyRestService,
            DropDownCustomFieldDefinitionRestService,
            TextCustomFieldDefinitionRestService,
            TextAreaCustomFieldDefinitionRestService,
            ImageURLCustomFieldDefinitionRestService,
            CheckBoxCustomFieldDefinitionRestService
        ].each { cls ->
            String clsName = cls.simpleName

            //assume the convention of the property name being the class name
            //with the first character downcased
            String property = clsName[0].toLowerCase() + clsName.substring(1)

            def mock = mockFor(cls)
            mock.demand.createFromDto(0..100) { dto, skipValidation ->

                dto.id = 10

                created << dto
                updateLists(dto)

                return dto
            }
            mock.demand.updateById(0..100) { id, dto, skipValidation ->
                assert id == dto.id

                updated << dto
                updateLists(dto)


                return dto
            }

            service[property] = mock.createMock()
            service[property].metaClass.created = []
            service[property].metaClass.updated = []
        }
    }

    //A very simple "persistence" mechanism for relevant classes
    List profileList, categoryList, typesList, stateList, customFieldDefinitionList,
        fieldValueList, serviceItemList, agencyList

    //The resolution mechanism relies on the Item's list method, so mock it to something we
    //control
    private mockListMethods() {
        profileList = []
        categoryList = []
        typesList = []
        stateList = []
        customFieldDefinitionList = []
        fieldValueList = []
        serviceItemList = []
        agencyList = []

        Profile.metaClass.'static'.list = { profileList }
        Category.metaClass.'static'.list = { categoryList }
        Types.metaClass.'static'.list = { typesList }
        State.metaClass.'static'.list = { stateList }
        CustomFieldDefinition.metaClass.'static'.list = { customFieldDefinitionList }
        Agency.metaClass.'static'.list = { agencyList }
    }

    private mockFindByMethods() {
        Agency.metaClass.'static'.findByTitle = { title ->
            def original = agencyList.find { it.title == title }
            if (original) {
                def copy = new Agency(original.properties)
                copy.editedDate = null

                return copy
            }
            else {
                return null
            }
        }
        Profile.metaClass.'static'.findByUsername = { username ->
            def original = profileList.find { it.username == username }
            if (original) {
                def copy = new Profile(original.properties)
                copy.editedDate = null

                return copy
            }
            else {
                return null
            }
        }
        Types.metaClass.'static'.findByUuid = { uuid ->
            def original = typesList.find { it.uuid == uuid }
            if (original) {
                def copy = new Types(original.properties)
                copy.editedDate = null

                return copy
            }
            else {
                return null
            }
        }
        Category.metaClass.'static'.findByUuid = { uuid ->
            def original = categoryList.find { it.uuid == uuid }
            if (original) {
                def copy = new Category(original.properties)
                copy.editedDate = null

                return copy
            }
            else {
                return null
            }
        }
        State.metaClass.'static'.findByUuid = { uuid ->
            def original = stateList.find { it.uuid == uuid }
            if (original) {
                def copy = new State(original.properties)
                copy.editedDate = null

                return copy
            }
            else {
                return null
            }
        }
        CustomFieldDefinition.metaClass.'static'.findByUuid = { uuid ->
            def original = customFieldDefinitionList.find { it.uuid == uuid }
            if (original) {
                def cls = original.class
                def copy = cls.metaClass.invokeConstructor(original.properties)
                copy.editedDate = null

                return copy
            }
            else {
                return null
            }
        }
        ServiceItem.metaClass.'static'.findByUuid = { uuid ->
            def original = serviceItemList.find { it.uuid == uuid }
            if (original) {
                def copy = new ServiceItem(original.properties)
                copy.editedDate = null

                return copy
            }
            else {
                return null
            }
        }
    }

    //add an object to the appropriate mocked list
    private void updateLists(obj) {
        List list

        if (obj instanceof Profile) {
            list = profileList
        }
        else if (obj instanceof Category) {
            list = categoryList
        }
        else if (obj instanceof Types) {
            list = typesList
        }
        else if (obj instanceof State) {
            list = stateList
        }
        else if (obj instanceof CustomFieldDefinition) {
            list = customFieldDefinitionList
        }
        else if (obj instanceof ServiceItem) {
            list = serviceItemList
        }
        else if (obj instanceof Agency) {
            list = agencyList
        }

        list?.add(obj)
    }

    void setUp() {
        FakeAuditTrailHelper.install()

        //can't re-use the same exact domain object between tests so make and save copies
        def interfaceConfig =
            new InterfaceConfiguration(InterfaceConfiguration.OMP_INTERFACE.properties)
                .save(failOnError: true)

        task = new ImportTask(
            name: 'Test Task',
            updateType: Constants.IMPORT_TYPE_DELTA,
            interfaceConfig: interfaceConfig
        )

        resetEditedDates()

        service.scheduledImportHttpService = mockHttpService()
        mockRestServices()
        mockListMethods()
        service.accountService = [
            loginSystemUser: {}
        ] as AccountService
        service.auditLoggingService = [
            logImport: {}
        ] as AuditLoggingService

        FieldValue.metaClass.'static'.dolist = {
            def dropDownCustomFieldDefinition = importData.customFieldDefs.find { cfd ->
                cfd instanceof DropDownCustomFieldDefinition
            }

            dropDownCustomFieldDefinition.fieldValues
        }
        FieldValue.metaClass.'static'.findByDisplayTextAndCustomFieldDefinition = { text, defn ->
            FieldValue.dolist().find { it.displayText == text }
        }
    }

    void testExecuteScheduledImportWithId() {
        def taskPassedIn
        long id = task.save(failOnError:true).id

        service.metaClass.executeScheduledImport = { ImportTask t ->
            taskPassedIn = t
        }

        service.executeScheduledImport(id)

        assert task.id == taskPassedIn.id
        assert task.name == taskPassedIn.name
    }

    void testCallsLogImport() {
        ImportTask passedInTask

        mockImportMethods([])

        service.auditLoggingService = [
            logImport: { passedInTask = it }
        ] as AuditLoggingService

        service.executeScheduledImport(task)

        assert passedInTask == task
    }
    
    @FreshRuntime
    void testImportProfiles() {
        mockImportMethods(['profiles'])

        service.executeScheduledImport(task)

        def profiles = service.profileRestService.created
        def usernames = profiles.collect { it.username }
        def result = task.lastRunResult

        assert result.result == true
        assert profiles.size() == 2
        assert usernames.contains('testAdmin1')
        assert usernames.contains('testUser1')
        assert !usernames.contains('System')    //System user does not come through in import

        makeUpdate()
        mockFindByMethods()

        service.executeScheduledImport(task)
        profiles = service.profileRestService.updated

        assert profiles.size() == 1
        // Check that the correct profile was updated based on System being removed
        assert profiles[0].username == importData.profiles[2].username
    }

    void testImportTypes() {
        mockImportMethods(['profiles', 'types'])

        service.executeScheduledImport(task)

        def types = service.typeRestService.created
        def profiles = service.profileRestService.created
        Profile admin = profiles.find { it.username == 'testAdmin1' }

        assert types.size() == 2

        assert types[0].is(importData.types[0])
        assert types[0].createdBy.is(admin)
        assert types[0].editedBy.is(admin)


        assert types[1].is(importData.types[1])
        assert types[1].createdBy.is(admin)
        assert types[1].editedBy.is(admin)

        //images are stripped out
        assert types.find { it.image != null } == null


        makeUpdate()
        mockFindByMethods()

        service.executeScheduledImport(task)
        types = service.typeRestService.updated

        assert types.size() == 1
        assert types[0].uuid == importData.types[1].uuid
    }

    void testImportCategories() {
        mockImportMethods(['profiles', 'categories'])

        service.executeScheduledImport(task)

        def categories = service.categoryRestService.created
        def profiles = service.profileRestService.created
        Profile admin = profiles.find { it.username == 'testAdmin1' }

        assert categories.size() == 2

        assert categories[0].is(importData.categories[0])
        assert categories[0].createdBy.is(admin)
        assert categories[0].editedBy.is(admin)


        assert categories[1].is(importData.categories[1])
        assert categories[1].createdBy.is(admin)
        assert categories[1].editedBy.is(admin)


        makeUpdate()
        mockFindByMethods()

        service.executeScheduledImport(task)
        categories = service.categoryRestService.updated

        assert categories.size() == 1
        assert categories[0].uuid == importData.categories[1].uuid
    }

    void testImportStates() {
        mockImportMethods(['profiles', 'states'])

        service.executeScheduledImport(task)

        def states = service.stateRestService.created
        def profiles = service.profileRestService.created
        Profile admin = profiles.find { it.username == 'testAdmin1' }

        assert states.size() == 2
        assert states[0].is(importData.states[0])
        assert states[1].is(importData.states[1])


        makeUpdate()
        mockFindByMethods()

        service.executeScheduledImport(task)
        states = service.stateRestService.updated

        assert states.size() == 1
        assert states[0].uuid == importData.states[1].uuid
    }

    void testImportCustomFields() {
        mockImportMethods(['profiles', 'types', 'customFieldDefinitions'])

        service.executeScheduledImport(task)

        def textCustomFieldDefinitions =
            service.textCustomFieldDefinitionRestService.created
        def textAreaCustomFieldDefinitions =
            service.textAreaCustomFieldDefinitionRestService.created
        def imageURLCustomFieldDefinitions =
            service.imageURLCustomFieldDefinitionRestService.created
        def dropDownCustomFieldDefinitions =
            service.dropDownCustomFieldDefinitionRestService.created
        def checkBoxCustomFieldDefinitions =
            service.checkBoxCustomFieldDefinitionRestService.created


        def profiles = service.profileRestService.created
        Profile admin = profiles.find { it.username == 'testAdmin1' }

        def types = service.typeRestService.created
        Types associatedType = types.find {
            it.uuid == '4f2681ef-20c0-4923-b95c-f85cf52b171c'
        }


        assert textCustomFieldDefinitions.size() == 1
        assert importData.customFieldDefs.contains(textCustomFieldDefinitions[0])
        assert textCustomFieldDefinitions[0].createdBy.is(admin)
        assert textCustomFieldDefinitions[0].editedBy.is(admin)
        assert textCustomFieldDefinitions[0].types[0].is(associatedType)

        assert textAreaCustomFieldDefinitions.size() == 1
        assert importData.customFieldDefs.contains(textAreaCustomFieldDefinitions[0])
        assert textAreaCustomFieldDefinitions[0].createdBy.is(admin)
        assert textAreaCustomFieldDefinitions[0].editedBy.is(admin)
        assert textAreaCustomFieldDefinitions[0].types[0].is(associatedType)

        assert checkBoxCustomFieldDefinitions.size() == 1
        assert importData.customFieldDefs.contains(checkBoxCustomFieldDefinitions[0])
        assert checkBoxCustomFieldDefinitions[0].createdBy.is(admin)
        assert checkBoxCustomFieldDefinitions[0].editedBy.is(admin)
        assert checkBoxCustomFieldDefinitions[0].types == []

        assert imageURLCustomFieldDefinitions.size() == 1
        assert importData.customFieldDefs.contains(imageURLCustomFieldDefinitions[0])
        assert imageURLCustomFieldDefinitions[0].createdBy.is(admin)
        assert imageURLCustomFieldDefinitions[0].editedBy.is(admin)
        assert imageURLCustomFieldDefinitions[0].types[0].is(associatedType)

        assert dropDownCustomFieldDefinitions.size() == 1
        assert importData.customFieldDefs.contains(dropDownCustomFieldDefinitions[0])
        assert dropDownCustomFieldDefinitions[0].createdBy.is(admin)
        assert dropDownCustomFieldDefinitions[0].editedBy.is(admin)
        assert dropDownCustomFieldDefinitions[0].types == []


        makeUpdate()
        mockFindByMethods()

        service.executeScheduledImport(task)

        textCustomFieldDefinitions = service.textCustomFieldDefinitionRestService.updated

        textAreaCustomFieldDefinitions = service.textAreaCustomFieldDefinitionRestService.updated

        checkBoxCustomFieldDefinitions = service.checkBoxCustomFieldDefinitionRestService.updated

        imageURLCustomFieldDefinitions = service.imageURLCustomFieldDefinitionRestService.updated

        dropDownCustomFieldDefinitions = service.dropDownCustomFieldDefinitionRestService.updated

        def customFieldDefinitions = textCustomFieldDefinitions +
            textAreaCustomFieldDefinitions + checkBoxCustomFieldDefinitions +
            imageURLCustomFieldDefinitions + dropDownCustomFieldDefinitions

        assert customFieldDefinitions.size() == 2
        assert customFieldDefinitions.contains(importData.customFieldDefs[1])
        assert customFieldDefinitions.contains(importData.customFieldDefs[3])
    }

    void testImportServiceItems() {
        mockImportMethods(['profiles', 'types', 'categories', 'states',
            'customFieldDefinitions', 'serviceItems'])

        service.executeScheduledImport(task)

        def serviceItems = service.serviceItemRestService.created
        def agencies = service.agencyRestService.created
        def profiles = service.profileRestService.created
        def types = service.typeRestService.created
        def states = service.stateRestService.created
        def categories = service.categoryRestService.created
        def customFieldDefinitions = [
            service.textCustomFieldDefinitionRestService.created,
            service.textAreaCustomFieldDefinitionRestService.created,
            service.checkBoxCustomFieldDefinitionRestService.created,
            service.imageURLCustomFieldDefinitionRestService.created,
            service.dropDownCustomFieldDefinitionRestService.created
        ].flatten()


        assert agencies.size() == 1
        assert agencies[0].is(importData.serviceItems[1].agency)

        assert serviceItems.size() == 2

        //get the list in the right order for the following tests
        if (serviceItems[0].title == 'Listing 2') {
            serviceItems = serviceItems.reverse()
        }

        assert serviceItems[0].createdBy.is(profiles.find { it.username == 'testAdmin1' })
        assert serviceItems[0].editedBy.is(profiles.find { it.username == 'testAdmin1' })
        assert serviceItems[0].types.is(types.find {
            it.uuid == '4f2681ef-20c0-4923-b95c-f85cf52b171c'
        })
        assert serviceItems[0].state.is(states.find {
            it.uuid == '4a5e7c6b-be13-46a4-9f7e-9a28935dd929'
        })
        assert serviceItems[0].categories.contains(categories.find {
            it.uuid == 'd7b501e2-9585-4ae6-8036-a19880b44921'
        })
        assert serviceItems[0].categories.size() == 2
        assert serviceItems[0].categories.contains(categories.find {
            it.uuid == '76f885d0-9ae7-49a6-ad95-2d929f7c5cf7'
        })
        assert serviceItems[0].owners.contains(profiles.find {
            it.username == 'testUser1'
        })

        assert serviceItems[0].customFields.find { cf ->
            cf instanceof TextCustomField &&
            cf.customFieldDefinition.is(customFieldDefinitions.find {
                it.uuid == '1958f80c-005c-4525-8147-d784af179dfd'
            }) &&
            cf.value == 'test value'
        }
        assert serviceItems[0].customFields.find { cf ->
            cf instanceof DropDownCustomField &&
            cf.customFieldDefinition.is(customFieldDefinitions.find { cfd ->
                cfd.uuid == 'c574e73a-6006-4899-959f-4f9b8a68e6e9'
            }) &&
            cf.customFieldDefinition.fieldValues.find { cf.value } &&
            cf.fieldValueText == 'value 1'
        }

        assert serviceItems[0].agency.is(agencies[0])


        assert serviceItems[1].createdBy.is(profiles.find { it.username == 'testAdmin1' })
        assert serviceItems[1].editedBy.is(profiles.find { it.username == 'testAdmin1' })
        assert serviceItems[1].types.is(types.find {
            it.uuid == 'e46c3676-61ba-4b9c-b6d9-c08dba622e47'
        })
        assert serviceItems[1].state.is(states.find {
            it.uuid == '5cee51ad-4324-4aa9-9928-e2c7303c1bdc'
        })
        assert serviceItems[1].categories.size() == 1
        assert serviceItems[1].categories.contains(categories.find {
            it.uuid == '76f885d0-9ae7-49a6-ad95-2d929f7c5cf7'
        })
        assert serviceItems[1].owners.contains(profiles.find {
            it.username == 'testAdmin1'
        })


        makeUpdate()
        mockFindByMethods()

        service.executeScheduledImport(task)
        serviceItems = service.serviceItemRestService.updated

        assert serviceItems.size() == 1
        assert serviceItems[0].uuid == importData.serviceItems[1].uuid
    }

    void testImportRelationships() {
        mockImportMethods(['profiles', 'types', 'categories', 'states',
            'customFieldDefinitions', 'serviceItems', 'relationships'])

        ServiceItem.metaClass.'static'.findByUuid = { uuid ->
            serviceItemList.find { it.uuid == uuid }
        }

        service.executeScheduledImport(task)

        def serviceItems = service.serviceItemRestService.created
        def parentItem = serviceItems.find { it.uuid == 'e520a2bc-9247-400d-8df8-ddd226853bd3' }

        assert parentItem.relationships.size() == 1
        assert parentItem.relationships[0].relationshipType == RelationshipType.REQUIRE
        assert parentItem.relationships[0].relatedItems.size() == 1
        assert parentItem.relationships[0].relatedItems[0] == serviceItems.find {
            it.uuid == '497df2c6-ffd7-49be-a7ab-000d9ec1ddf8'
        }
    }

    void testImportStatus() {
        ImportStatus importStatus = new ImportStatus()

        //for use in the update phase of the test
        ImportStatus anotherImportStatus = new ImportStatus()

        //instead of a new importstatus return the one we have here in the test
        ImportStatus.metaClass.constructor = { importStatus }

        ServiceItem.metaClass.'static'.findByUuid = { uuid ->
            serviceItemList.find { it.uuid == uuid }
        }

        service.executeScheduledImport(task)

        assert importStatus.success

        assert importStatus.categories.created == 2
        assert importStatus.categories.updated == 0
        assert importStatus.categories.notUpdated == 0

        assert importStatus.types.created == 2
        assert importStatus.types.updated == 0
        assert importStatus.types.notUpdated == 0

        assert importStatus.states.created == 2
        assert importStatus.states.updated == 0
        assert importStatus.states.notUpdated == 0

        assert importStatus.customFieldDefs.created == 5
        assert importStatus.customFieldDefs.updated == 0
        assert importStatus.customFieldDefs.notUpdated == 0

        assert importStatus.profiles.created == 2
        assert importStatus.profiles.updated == 0
        assert importStatus.profiles.notUpdated == 0

        assert importStatus.serviceItems.created == 2
        assert importStatus.serviceItems.updated == 0
        assert importStatus.serviceItems.notUpdated == 0

        assert importStatus.agencies.created == 1
        assert importStatus.agencies.updated == 0
        assert importStatus.agencies.notUpdated == 0

        ImportTaskResult result = task.lastRunResult
        assert result.message == importStatus.summaryMessage

        makeUpdate()
        mockFindByMethods()

        importStatus = anotherImportStatus

        service.executeScheduledImport(task)

        assert importStatus.success

        assert importStatus.categories.created == 0
        assert importStatus.categories.updated == 1
        assert importStatus.categories.notUpdated == 1

        assert importStatus.types.created == 0
        assert importStatus.types.updated == 1
        assert importStatus.types.notUpdated == 1

        assert importStatus.states.created == 0
        assert importStatus.states.updated == 1
        assert importStatus.states.notUpdated == 1

        assert importStatus.customFieldDefs.created == 0
        assert importStatus.customFieldDefs.updated == 2
        assert importStatus.customFieldDefs.notUpdated == 3

        assert importStatus.profiles.created == 0
        assert importStatus.profiles.updated == 1
        assert importStatus.profiles.notUpdated == 1

        assert importStatus.serviceItems.created == 0
        assert importStatus.serviceItems.updated == 1
        assert importStatus.serviceItems.notUpdated == 1

        assert importStatus.agencies.created == 0
        assert importStatus.agencies.updated == 0
        assert importStatus.agencies.notUpdated == 1

        result = task.lastRunResult
        assert result.message == importStatus.summaryMessage
    }

    void testImportStatusRelationships() {
        ImportStatus importStatus = new ImportStatus()

        //for use in the update phase of the test
        ImportStatus anotherImportStatus = new ImportStatus()

        ImportStatus.metaClass.constructor = { importStatus }

        ServiceItem.metaClass.'static'.findByUuid = { uuid ->
            serviceItemList.find { it.uuid == uuid }
        }

        service.executeScheduledImport(task)

        assert importStatus.relationships.created == 2
        assert importStatus.relationships.updated == 0
        assert importStatus.relationships.notUpdated == 0

        ImportTaskResult result = task.lastRunResult
        assert result.message == importStatus.summaryMessage

        //makeUpdate()

        //serviceItemList.each { si ->
            //si.relationships?.each { rel -> rel.metaClass.clear = {} }
        //}

        //importStatus = anotherImportStatus

        //service.executeScheduledImport(task)

        //assert importStatus.relationships.created == 0
        //assert importStatus.relationships.updated == 0
        //assert importStatus.relationships.notUpdated == 1

        //result = task.lastRunResult
        //assert result.message == importStatus.summaryMessage
    }

    @FreshRuntime
    void testExceptions() {
        ImportStatus importStatus = new ImportStatus()

        //def save = ImportStatus.metaClass.constructor

        //instead of a new importstatus return the one we have here in the test
        ImportStatus.metaClass.constructor = { importStatus }

        ServiceItem.metaClass.'static'.findByUuid = { uuid ->
            serviceItemList.find { it.uuid == uuid }
        }

        service.categoryRestService.metaClass.createFromDto = { dto, skipValidation ->
            throw new Exception("Category exception")
        }

        service.typeRestService.metaClass.createFromDto = { dto, skipValidation ->
            throw new ValidationException("Types exception", )
        }

        service.stateRestService.metaClass.createFromDto = { dto, skipValidation ->
            throw new Exception("State exception")
        }

        service.profileRestService.metaClass.createFromDto = { dto, skipValidation ->
            throw new Exception("Profile exception")
        }

        service.serviceItemRestService.metaClass.createFromDto = { dto, skipValidation ->
            throw new IllegalArgumentException("ServiceItem exception")
        }

        //relationships will fail since serviceItems are missing

        service.executeScheduledImport(task)

        assert importStatus.success == false

        assert importStatus.categories.created == 0
        assert importStatus.categories.failed == 2
        assert importStatus.categories.updated == 0
        assert importStatus.categories.notUpdated == 0

        assert importStatus.types.created == 0
        assert importStatus.types.failed == 2
        assert importStatus.types.updated == 0
        assert importStatus.types.notUpdated == 0

        assert importStatus.states.created == 0
        assert importStatus.states.failed == 2
        assert importStatus.states.updated == 0
        assert importStatus.states.notUpdated == 0

        //we aren't currently testing customFieldDef failures
        assert importStatus.customFieldDefs.created == 5
        assert importStatus.customFieldDefs.failed == 0
        assert importStatus.customFieldDefs.updated == 0
        assert importStatus.customFieldDefs.notUpdated == 0

        assert importStatus.profiles.created == 0
        assert importStatus.profiles.failed == 2
        assert importStatus.profiles.updated == 0
        assert importStatus.profiles.notUpdated == 0

        assert importStatus.serviceItems.created == 0
        assert importStatus.serviceItems.failed == 2
        assert importStatus.serviceItems.updated == 0
        assert importStatus.serviceItems.notUpdated == 0

        assert importStatus.relationships.created == 0
        assert importStatus.relationships.failed == 1
        assert importStatus.relationships.updated == 0
        assert importStatus.relationships.notUpdated == 0

        assert importStatus.agencies.created == 1
        assert importStatus.agencies.failed == 0
        assert importStatus.agencies.updated == 0
        assert importStatus.agencies.notUpdated == 0

        ImportTaskResult result = task.lastRunResult
        assert result.message == importStatus.summaryMessage

        //ImportStatus.metaClass.constructor = save 
    }

    @FreshRuntime
    void testHttpServiceException() {
        ImportStatus importStatus = new ImportStatus()

        //instead of a new importstatus return the one we have here in the test
        ImportStatus.metaClass.constructor = { importStatus }

        def exceptionMessage = "Could not complete request"

        service.scheduledImportHttpService.metaClass.retrieveRemoteImportData = { ImportTask t ->
            throw new IOException(exceptionMessage)
        }

        service.executeScheduledImport(task)

        assert importStatus.success == false
        assert importStatus.messages[0].contains(exceptionMessage)

        assert importStatus.categories.created == 0
        assert importStatus.categories.failed == 0
        assert importStatus.categories.updated == 0
        assert importStatus.categories.notUpdated == 0

        assert importStatus.types.created == 0
        assert importStatus.types.failed == 0
        assert importStatus.types.updated == 0
        assert importStatus.types.notUpdated == 0

        assert importStatus.states.created == 0
        assert importStatus.states.failed == 0
        assert importStatus.states.updated == 0
        assert importStatus.states.notUpdated == 0

        assert importStatus.customFieldDefs.created == 0
        assert importStatus.customFieldDefs.failed == 0
        assert importStatus.customFieldDefs.updated == 0
        assert importStatus.customFieldDefs.notUpdated == 0

        assert importStatus.profiles.created == 0
        assert importStatus.profiles.failed == 0
        assert importStatus.profiles.updated == 0
        assert importStatus.profiles.notUpdated == 0

        assert importStatus.serviceItems.created == 0
        assert importStatus.serviceItems.failed == 0
        assert importStatus.serviceItems.updated == 0
        assert importStatus.serviceItems.notUpdated == 0

        assert importStatus.relationships.created == 0
        assert importStatus.relationships.failed == 0
        assert importStatus.relationships.updated == 0
        assert importStatus.relationships.notUpdated == 0

        assert importStatus.agencies.created == 0
        assert importStatus.agencies.failed == 0
        assert importStatus.agencies.updated == 0
        assert importStatus.agencies.notUpdated == 0

        ImportTaskResult result = task.lastRunResult
        assert result.message == importStatus.summaryMessage
    }
}
