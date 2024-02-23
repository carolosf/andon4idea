# Andon IntelliJ Plugin
`Andon (English: ‘Sign’ or ‘Signal’): A visual aid that highlights where action is required.` [Andon](https://mag.toyota.co.uk/andon-toyota-production-system/)

The Andon IntelliJ/Idea plugin is a visual aid that informs Jetbrains IDE users when an action is required.

## Example usages
Informing users of a monorepo that there is a build error in the main build pipeline so that they can take the action of reverting the breaking code or fixing it.

Informing users of a monorepo that there is a build error in slow integration tests.

Informing users of a small repo that there is a build error.

## Requirements

The plugin requires that these environment variables are set 

Feel free to suggest other ways of setting them with pull requests - alternatively hard code these values and package your own plugin - MIT license allows this

```text
ANDON_PLUGIN_TEAM_NAME - required if informing teams that their build is broken
ANDON_PLUGIN_DISCOVERY_URL - the URL of the server you will implement to inform users that actions are required 
ANDON_PLUGIN_POLL_TIME_SECONDS - defaults to 300 seconds (5 minutes) change if higher resolution is required. Default chosen to prevent DDOSing your server with many users.
```

The username of the logged in user is used for determining if a particular user needs to take an action.

# Server API
There are two parts
## Alert Groups Configuration
This endpoint allows the plugin to discover groups of alerts that you want to take action on.
It needs to point to the status update pages for each alert.
An example output can be found in [testdata/alertgroups.json](testdata/alertgroups.json)

## Alert Group Status
This is the status updater for a particular alert group.
An example output can be found in [testdata/main.json](testdata/main.json) and [testdata/slow.json](testdata/slow.json)

The goal of the way this is structured is so that everything can be controlled by the server with minimal updates to the plugin.

Field descriptions:
```text
isHealthy - true/false - is current status healthy
rateLimitInSeconds - default 300 - how frequently to annoy plugin users about this problem
blamedUsers - a list containing the usernames of the users that may have contributed when isHealthy is false
blamedTeams - a list containing the team names that may have contributed when isHealthy is false

message to display to blamed users if set, otherwise won't notify blamed users:
userBlameText - message box body
userBlameTitle - message box title 
userBlameIcon - default warn - message box icon possible values are one of ["warn", "error", "info", "question"]

message to display to blamed teams if set, otherwise won't notify blamed teams:
teamBlameText - message box body
teamBlameTitle - message box title 
teamBlameIcon - default warn - message box icon possible values are one of ["warn", "error", "info", "question"]

message to display to everyone using the plugin if set, otherwise won't notify everyone using the plugin:
genericBlameText - message box body
genericBlameTitle - message box title 
genericBlameIcon - default warn - message box icon possible values are one of ["warn", "error", "info", "question"]
```

Setting the above selectively lets you notify some users or everyone about some alert groups while others only get notified about other alert groups.

# Running example server
In the `testdata` directory there is a server.sh script that starts a python server and serves the files in the testdata directory.
```shell
cd testdata
./server.sh
```


## Icons from
[fire](https://www.svgrepo.com/svg/396473/fire)
COLLECTION: Noto Emojis
LICENSE: Apache License
AUTHOR: googlefonts

[green-circle](https://www.svgrepo.com/svg/396579/green-circle)
COLLECTION: Noto Emojis
LICENSE: Apache License
AUTHOR: googlefonts

## Inspiration for writing the plugin
[git4idea](https://github.com/JetBrains/intellij-community/blob/master/plugins/git4idea/src/git4idea/ui/branch/GitBranchWidget.kt)
