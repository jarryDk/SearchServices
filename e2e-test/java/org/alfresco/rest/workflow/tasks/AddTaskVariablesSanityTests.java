package org.alfresco.rest.workflow.tasks;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestVariableModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TaskModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author iulia.cojocea
 */
@Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
public class AddTaskVariablesSanityTests extends RestTest
{
    private UserModel userModel, userWhoStartsTask;
    private SiteModel siteModel;
    private FileModel fileModel;
    private UserModel assigneeUser;
    private TaskModel taskModel;
    private RestVariableModel restVariablemodel;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        userModel = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        fileModel = dataContent.usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        userWhoStartsTask = dataUser.createRandomTestUser();
        assigneeUser = dataUser.createRandomTestUser();
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assigneeUser);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY,
            description = "Create non-existing task variable with admin")
    public void createTaskVariableWithAdmin() throws Exception
    {
        UserModel adminUser = dataUser.getAdminUser();
        restClient.authenticateUser(adminUser);
        RestVariableModel variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        
        restVariablemodel = restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restVariablemodel.assertThat()
             .field("scope").is(variableModel.getScope())
             .and().field("name").is(variableModel.getName())
             .and().field("value").is(variableModel.getValue())
             .and().field("type").is(variableModel.getType());
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY,
            description = "Create non-existing task variable with user involved in the process")
    public void createTaskVariableWithInvolvedUser() throws Exception
    {
        restClient.authenticateUser(assigneeUser);
        RestVariableModel variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        
        restVariablemodel = restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restVariablemodel.assertThat().field("scope").is(variableModel.getScope())
             .and().field("name").is(variableModel.getName())
             .and().field("value").is(variableModel.getValue())
             .and().field("type").is(variableModel.getType());
        
        restClient.withWorkflowAPI().usingTask(taskModel).getTaskVariables().assertThat().entriesListContains("name", variableModel.getName());
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY,
            description = "Create non-existing task variable with task owner")
    public void createTaskVariableWithTaskOwner() throws Exception
    {
        restClient.authenticateUser(userWhoStartsTask);
        RestVariableModel variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        
        restVariablemodel = restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restVariablemodel.assertThat().field("scope").is(variableModel.getScope())
             .and().field("name").is(variableModel.getName())
             .and().field("value").is(variableModel.getValue())
             .and().field("type").is(variableModel.getType());
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY,
            description = "Create non-existing task variable with any user")
    public void createTaskVariableWithRandomUser() throws Exception
    {
        userModel = dataUser.createRandomTestUser();
        restClient.authenticateUser(userModel);
        RestVariableModel variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        
        restVariablemodel = restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary("Permission was denied");
    }
}
