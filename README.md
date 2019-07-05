# ServiceNow API Jenkins Plugin

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/service-now-plugin/master)](https://ci.jenkins.io/job/Plugins/job/service-now-plugin/job/master/)[![Maintainability](https://api.codeclimate.com/v1/badges/2e3da6a8c0e1260ce7fb/maintainability)](https://codeclimate.com/github/jenkinsci/service-now-plugin/maintainability)

This is a Jenkins plugin that allows a workflow step
to automatically build API requests for the ServiceNow API and return appropriately parsed
values based on the type of API called.

It defines a set of configuration values that can be used to identify the ServiceNow instance
information and configure the API requests.

## Jenkins-ServiceNow Change Workflow

The central idea for this plugin is to allow the deployment 
process through Jenkins to manageeverything about a service 
now change. One of the first 
pieces from ServiceNow this plugin uses is the idea of a 
standard change producer. This allows teams to build change 
requests templates that their application uses when it is 
deployed.

Once these templates are created, the goal is to allow Jenkins 
to carry out everything necessary for the change request, 
from creation and data gathering (test results, commit messages, 
and approvals), to managing the change tasks within the change 
itself, all the way to closing the change request once it 
completes. This is implemented using the Step mechanism that 
is available to the Jenkins pipeline.

A standard change workflow can vary based on the ServiceNow
implementation at your company. In general, there are pre-tasks,
implementation tasks, and post-tasks, as well as change metadata.
This plugin provides interfaces to interact with these tasks,
update change metadata, proceed task workflows, and attach
information from the build to proper locations within the
context of a change request. 

## Plugin Usage

### All Steps

#### Required Parameters

Every step must provide these two arguments in order to connect to the ServiceNow instance

* `serviceNowConfiguration`
  * `instance` - Instance of service-now to connect to (https://<instance>.service-now.com)
* `credentialsId` - Jenkins credentials for Username with Password credentials or Vault Role Credentials, if including `vaultConfiguration` below

#### Optional Parameters

`vaultConfiguration`
* `url` - Vault url
* `path` - Vault path to get authentication values

### `serviceNow_createChange`

#### Required Parameters

`serviceNowConfiguration`
* `producerId` - Producer ID of the standard change to create


#### Response

`result`
* `sys_id`
* `number`
* `record`
* `redirect_portal_url`
* `redirect_url`
* `table`
* `redirect_to`

#### Example
Create a service-now change and capture the sys_id and change number
```groovy
def response = serviceNow_createChange serviceNowConfiguration: [instance: 'exampledev', producerId: 'ls98y3khifs8oih3kjshihksjd'], credentialsId: 'jenkins-vault', vaultConfiguration: [url: 'https://vault.example.com:8200', path: 'secret/for/service_now/']
def jsonSlurper = new JsonSlurper()
def createResponse = jsonSlurper.parseText(response.content)
def sysId = createResponse.result.sys_id
def changeNumber = createResponse.result.number
```

### `serviceNow_updateChangeItem`

#### Required Parameters

`serviceNowItem`
* `table` - Table of the item to be updated (ex. change_request, change_task)
* `sysId` - SysId of the item to be updated
* `body` - Json message (as String) of the properties and values to be updated

#### Response

`result`

#### Example
Update a service-now change with a short description and description
```groovy
def messageJson = new JSONObject()
messageJson.putAll([
                short_description: 'My change',
                description: 'My longer description of the change'
        ])
def response = serviceNow_updateChangeItem serviceNowConfiguration: [instance: 'exampledev'], credentialsId: 'jenkins-vault', serviceNowItem: [table: 'change_request', sysId: 'adg98y29thukwfd97ihu23', body: messageJson.toString()], vaultConfiguration: [url: 'https://vault.example.com:8200', path: 'secret/for/service_now/']

```

### `serviceNow_getChangeState`

#### Required Parameters

`serviceNowItem`
* `sysId` - SysId of the change request

#### Response

`state` - String representation of the state (@see [ServiceNowStates](src/main/java/org/jenkinsci/plugins/servicenow/util/ServiceNowStates.java))

#### Example
Get the current state of a service-now change
```groovy
def response = serviceNow_UpdateChangeItem serviceNowConfiguration: [instance: 'exampledev'], credentialsId: 'jenkins-vault', serviceNowItem: [sysId: 'adg98y29thukwfd97ihu23'], vaultConfiguration: [url: 'https://vault.example.com:8200', path: 'secret/for/service_now/']
echo response //NEW
```

### `serviceNow_getCTask`

#### Required Parameters

`serviceNowItem`
* `sysId` - SysId change to get CTask from
* `ctask` - String description of the ctask for querying (optional)

#### Response

`result` - an array of results
* `sys_id`
* `number`
* `short_description`

#### Example
Get ctask of a service-now change
```groovy
def response = serviceNow_getCTask serviceNowConfiguration: [instance: 'exampledev'], credentialsId: 'jenkins-vault', serviceNowItem: [sysId: 'agsdh0wehosid9723h30h', ctask: 'UAT_TESTING'], vaultConfiguration: [url: 'https://vault.example.com:8200', path: 'secret/for/service_now/']
def ctaskResponse = new JsonSlurper().parseText(response.content)
def ctaskSysId = createResponse.result[0].sys_id
def ctaskNumber = createResponse.result[0].number
```

### `serviceNow_updateTask`

There is often a desire to update a change task based on certain
fields in the task. For example, you could update all planning tasks to closed 
based on their type or update a single task that matches a specific field.

This function will run either a query or search for a ctask name,
based on the fields set on the `serviceNowItem` parameter. If there is 
a `ctask` field set, then it will not add the `query` string, as a query is intended
to be more broad that the `ctask` field.

#### Required Parameters

`serviceNowItem`
* `sysId` - SysId change to get CTask from

#### Optional Parameters

Must specify one of

`serviceNowItem`
* `ctask` - String description of the ctask for querying (optional)
* `query` - A more generic ServiceNow query to locate the task

#### Response

`result` - a list of ctask sys_ids that were updated

#### Example
Update all planning tasks to in progress state

```groovy
serviceNow_updateTask serviceNowConfiguration: [instance: 'exampledev'],
  credentialsId: 'servicenow',
  serviceNowItem: [sysId: 'fd03ba34db4f9f40ec45b2bd2b96197d',
    query: 'change_task_type=planning',
    body: '{"state":"2"}'
  ]
```



### `serviceNow_attachFile`

#### Required Parameters

`serviceNowItem`
* `sysId` - SysId to attach to (can be Change Request or CTask)
* `body` - String body of file to attach
* `filename` - Filename to upload into ServiceNow

#### Response

`result`

#### Example
Attach file to item in service-now
```groovy
def myFile = readFile file: 'my-test-file.txt'
serviceNow_attachFile serviceNowConfiguration: [instance: 'exampledev'], credentialsId: 'jenkins-vault', serviceNowItem: [sysId: 'agsdh0wehosid9723h30h', body: myFile, filename: 'my-test-file.txt'], vaultConfiguration: [url: 'https://vault.example.com:8200', path: 'secret/for/service_now/']
```

### `serviceNow_attachZip`

Attach a zip file (from the current directory) to a service-now item

#### Required Parameters

`serviceNowItem`
* `sysId` - SysId to attach to (can be Change Request or CTask)
* `filename` - Filename of the zip file to attach

#### Response

`result`

#### Example
Attach zip to item in service-now
```groovy
zip zipFile: 'my-zip-file.zip', glob: '*.txt'
serviceNow_attachZip serviceNowConfiguration: [instance: 'exampledev'], credentialsId: 'jenkins-vault', serviceNowItem: [sysId: 'agsdh0wehosid9723h30h', filename: 'my-zip-file.zip'], vaultConfiguration: [url: 'https://vault.example.com:8200', path: 'secret/for/service_now/']
```
