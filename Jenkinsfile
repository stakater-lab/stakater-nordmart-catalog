#!/usr/bin/env groovy
@Library('github.com/stakater/stakater-pipeline-library@revamp') _

releaseMavenApp {
    gitUser = "stakater-user"
    gitEmail = "stakater@gmail.com"
    usePersonalAccessToken = true
    tokenCredentialID = 'GithubToken'
    appName = "catalog"
    notifySlack = false
    runIntegrationTest = false
    dockerRepositoryURL = 'docker-release.workshop.stakater.com:443'
    javaRepositoryURL = 'http://nexus.release/repository/maven'
}
